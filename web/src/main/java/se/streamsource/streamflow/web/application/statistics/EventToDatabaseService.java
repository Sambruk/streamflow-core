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
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventPublisher;
import se.streamsource.streamflow.infrastructure.event.EventSubscriber;
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
@Mixins(EventToDatabaseService.EventToDatabaseMixin.class)
public interface EventToDatabaseService
        extends EventSubscriber, ServiceComposite
{
    class EventToDatabaseMixin
            implements EventSubscriber, Activatable
    {
        @Service
        EventPublisher publisher;

        @Service
        FileConfiguration fileConfig;

        @Service
        DataSource dataSource;

        @Structure
        UnitOfWorkFactory uowf;
        public Logger logger;
        private Properties sql;

        public void activate() throws Exception
        {
            logger = Logger.getLogger(EventToDatabaseService.class.getName());

            sql = new Properties();
            InputStream asStream = getClass().getResourceAsStream("statistics.properties");
            sql.load(asStream);

            // Create tables
            Connection conn = dataSource.getConnection();

            try
            {
                DatabaseMetaData dmd = conn.getMetaData();

                Set<String> createTables = new HashSet<String>();
                createTables.addAll(asList("completed"));

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

            publisher.subscribe(this);
        }

        public void passivate() throws Exception
        {
            publisher.unsubscribe(this);
        }

        public synchronized void notifyEvents(Iterable<DomainEvent> events)
        {
            Connection conn = null;
            try
            {
                UnitOfWork uow = null;
                try
                {
                    for (DomainEvent event : events)
                    {
                        if (event.name().get().equals("completed"))
                        {
                            if (uow == null)
                                uow = uowf.newUnitOfWork();

                            TaskEntity task = uow.get(TaskEntity.class, event.entity().get());
                            if (conn == null)
                            {
                                conn = dataSource.getConnection();
                            }

                            PreparedStatement stmt = conn.prepareStatement(sql.getProperty("completed.insert"));
                            stmt.setString(1, task.identity().get());
                            stmt.setDate(2, new java.sql.Date(task.createdOn().get().getTime()));
                            stmt.setDate(3, new java.sql.Date(event.on().get().getTime()));
                            stmt.setLong(4, event.on().get().getTime() - task.createdOn().get().getTime());
                            stmt.executeUpdate();
                            stmt.close();
                        }
                    }
                } catch (SQLException e)
                {
                    logger.log(Level.SEVERE, "Could not log statistics", e);
                }

                if (uow != null)
                    uow.discard();
            } finally
            {
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
