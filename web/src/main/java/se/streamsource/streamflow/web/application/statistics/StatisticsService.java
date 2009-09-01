/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.application.statistics;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.EventQuery;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.label.LabelEntity;
import se.streamsource.streamflow.web.domain.label.Labelable;
import se.streamsource.streamflow.web.domain.project.Members;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.ProjectOrganization;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.task.Owner;
import se.streamsource.streamflow.web.domain.task.TaskEntity;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static java.util.Arrays.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
@Mixins(StatisticsService.StatisticsMixin.class)
public interface StatisticsService
        extends EventSourceListener, ServiceComposite
{
    class StatisticsMixin
            implements EventSourceListener, Activatable
    {
        @Service
        EventSource source;

        @Service
        DataSource dataSource;

        @Structure
        UnitOfWorkFactory uowf;

        public Logger logger;
        private Properties sql;

        private String lastId;

        public void activate() throws Exception
        {
            logger = Logger.getLogger(StatisticsService.class.getName());

            sql = new Properties();
            InputStream asStream = getClass().getResourceAsStream("statistics.properties");
            sql.load(asStream);

            // Create tables
            Connection conn = dataSource.getConnection();

            try
            {
                DatabaseMetaData dmd = conn.getMetaData();

                Set<String> createTables = new HashSet<String>();
                createTables.addAll(asList("completed", "labels"));

                ResultSet tables = dmd.getTables(null, null, "%", null);
                while (tables.next())
                {
                    String tableName = tables.getString("TABLE_NAME").toLowerCase();
                    createTables.remove(tableName);
                }
                tables.close();

                for (String createTable : createTables)
                {
                    String create = sql.getProperty(createTable + ".create");
                    try
                    {
                        boolean ok = conn.createStatement().execute(create);
                    } catch (SQLException e)
                    {
                        throw e;
                    }
                }
            } finally
            {
                conn.close();
            }

            source.registerListener(this, new EventQuery(null, "completed", null, null));
        }

        public void passivate() throws Exception
        {
            source.unregisterListener(this);
        }

        public void eventsAvailable(EventSource source, EventSpecification specification)
        {
            Iterable<DomainEvent> events = null; // source.events(specification, lastId);

            Connection conn = null;
            UnitOfWork uow = null;
            String newLastId = null;
            try
            {
                for (DomainEvent event : events)
                {
                    if (uow == null)
                        uow = uowf.newUnitOfWork();

                    TaskEntity task = uow.get(TaskEntity.class, event.entity().get());

                    // Only save statistics for tasks in projects
                    if (task.owner().get() instanceof Project)
                    {

                        if (conn == null)
                        {
                            conn = dataSource.getConnection();
                        }

                        PreparedStatement stmt = conn.prepareStatement(sql.getProperty("completed.insert"));
                        int idx = 1;
                        String id = task.identity().get();
                        stmt.setString(idx++, id);
                        stmt.setString(idx++, task.taskId().get());
                        stmt.setString(idx++, task.description().get());
                        stmt.setDate(idx++, new java.sql.Date(task.createdOn().get().getTime()));
                        stmt.setDate(idx++, new java.sql.Date(event.on().get().getTime()));
                        stmt.setLong(idx++, event.on().get().getTime() - task.createdOn().get().getTime());
                        Assignee assignee = task.assignedTo().get();
                        stmt.setString(idx++, assignee.getDescription());
                        stmt.setString(idx++, task.owner().get().getDescription());
                        Owner owner = task.owner().get();
                        ProjectOrganization.ProjectOrganizationState po = (ProjectOrganization.ProjectOrganizationState) owner;
                        Describable.DescribableState organizationalUnit = (Describable.DescribableState) po.organizationalUnit().get();

                        // Figure out which group the user belongs to
                        Participant.ParticipantState participant = (Participant.ParticipantState) assignee;
                        String groupName = null;
                        findgroup: for (Group group : participant.groups())
                        {
                            Members.MembersState members = (Members.MembersState) owner;
                            if (members.isMember(group))
                            {
                                groupName = group.getDescription();
                                break findgroup;
                            }
                        }

                        stmt.setString(idx++, groupName);
                        stmt.setString(idx, organizationalUnit.description().get());

                        stmt.executeUpdate();
                        stmt.close();

                        // Add Label information
                        Labelable.LabelableState labelable = task;
                        for (LabelEntity labelEntity : labelable.labels())
                        {
                            stmt = conn.prepareStatement(sql.getProperty("labels.insert"));
                            stmt.setString(1, id);
                            stmt.setString(2, labelEntity.description().get());
                            stmt.executeUpdate();
                            stmt.close();
                        }

                        newLastId = event.identity().get();
                    }
                }

            } catch (Exception e)
            {
                logger.log(Level.SEVERE, "Could not log statistics", e);
                newLastId = null;
                if (conn != null)
                {
                    try
                    {
                        conn.rollback();
                    } catch (SQLException e1)
                    {
                        logger.log(Level.SEVERE, "Could not lrollback", e);
                    }
                }
            }finally
            {
                if (newLastId != null)
                    lastId = newLastId;

                if (uow != null)
                {
                    uow.discard();
                }

                if (conn != null)
                {
                    try
                    {
                        conn.close();
                    } catch (SQLException e)
                    {
                        // Ignore
                    }
                }
            }
        }
    }
}
