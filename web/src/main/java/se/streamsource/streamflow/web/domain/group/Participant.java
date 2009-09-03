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

package se.streamsource.streamflow.web.domain.group;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.web.domain.project.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(Participant.ParticipantMixin.class)
public interface Participant
        extends Identity, Describable
{
    void addProject(Project project);

    void removeProject(Project project);

    /**
     * Return all projects that this participant has access to.
     * This includes projects that this participant is transitively
     * a member of through groups.
     *
     * @return all projects that this participant is a member of
     */
    Iterable<Project> allProjects();

    void addGroup(Group group);

    void removeGroup(Group group);

    /**
     * Return all groups that this participant is a member of, transitively.
     *
     * @return all groups that this participant is a member of
     */
    Iterable<Group> allGroups();

    interface ParticipantState
    {
        ManyAssociation<Project> projects();

        ManyAssociation<Group> groups();
    }

    abstract class ParticipantMixin
            implements Participant
    {
        @Structure
        Module module;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        Participant participant;

        @This
        ParticipantState state;

        public void addProject(Project project)
        {
            state.projects().add(project);
        }

        public void removeProject(Project project)
        {
            state.projects().remove(project);
        }

        public Iterable<Project> allProjects()
        {
            List<Project> projects = new ArrayList<Project>();
            // List my own
            for (Project project : state.projects())
            {
                if (!projects.contains(project))
                    projects.add(project);
            }

            // Get group projects
            for (Group group : state.groups())
            {
                Iterable<Project> groupProjects = group.allProjects();
                for (Project groupProject : groupProjects)
                {
                    if (!projects.contains(groupProject))
                        projects.add(groupProject);
                }
            }

            return projects;
        }

        public void addGroup(Group group)
        {
            state.groups().add(group);
        }

        public void removeGroup(Group group)
        {
            state.groups().remove(group);
        }

        public Iterable<Group> allGroups()
        {
            List<Group> groups = new ArrayList<Group>();
            for (Group group : state.groups())
            {
                if (!groups.contains(group))
                    groups.add(group);

                // Add transitively
                for (Group group1 : group.allGroups())
                {
                    if (!groups.contains(group1))
                        groups.add(group);
                }
            }

            return groups;
        }
    }
}
