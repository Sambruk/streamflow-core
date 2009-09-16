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
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.domain.roles.Removable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.project.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@SideEffects(Participant.RemovableSideEffect.class)
@Mixins(Participant.ParticipantMixin.class)
public interface Participant
        extends Identity, Describable
{
    void joinProject(Project project);

    void leaveProject(Project project);

    void joinGroup(Group group);

    void leaveGroup(Group group);

    interface ParticipantState
    {
        ManyAssociation<Project> projects();

        ManyAssociation<Group> groups();

        /**
         * Return all projects that this participant has access to.
         * This includes projects that this participant is transitively
         * a member of through groups.
         *
         * @return all projects that this participant is a member of
         */
        Iterable<Project> allProjects();

        /**
         * Return all groups that this participant is a member of, transitively.
         *
         * @return all groups that this participant is a member of
         */
        Iterable<Group> allGroups();

        
        void joinedProject(DomainEvent event, Project project);


        void leftProject(DomainEvent event, Project project);


        void joinedGroup(DomainEvent event, Group group);


        void leftGroup(DomainEvent event, Group group);
    }

    abstract class ParticipantMixin
            implements Participant, ParticipantState
    {
        @Structure
        Module module;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        Participant participant;

        @This
        ParticipantState state;

        public void joinProject(Project project)
        {
            if (state.projects().contains(project))
                return;

            joinedProject(DomainEvent.CREATE, project);
        }

        public void leaveProject(Project project)
        {
            if (!state.projects().contains(project))
                return;

            leftProject(DomainEvent.CREATE, project);
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
                Iterable<Project> groupProjects = ((ParticipantState)group).allProjects();
                for (Project groupProject : groupProjects)
                {
                    if (!projects.contains(groupProject))
                        projects.add(groupProject);
                }
            }

            return projects;
        }

        public void joinGroup(Group group)
        {
            joinedGroup(DomainEvent.CREATE, group);
        }

        public void leaveGroup(Group group)
        {
            leftGroup(DomainEvent.CREATE, group);
        }

        public Iterable<Group> allGroups()
        {
            List<Group> groups = new ArrayList<Group>();
            for (Group group : state.groups())
            {
                if (!groups.contains(group))
                    groups.add(group);

                // Add transitively
                for (Group group1 : ((ParticipantState)group).allGroups())
                {
                    if (!groups.contains(group1))
                        groups.add(group);
                }
            }

            return groups;
        }

        public void joinedProject(DomainEvent event, Project project)
        {
            state.projects().add(project);
        }

        public void leftProject(DomainEvent event, Project project)
        {
            state.projects().remove(project);
        }

        public void joinedGroup(DomainEvent event, Group group)
        {
            state.groups().add(group);
        }

        public void leftGroup(DomainEvent event, Group group)
        {
            state.groups().remove(group);
        }
    }

    class RemovableSideEffect
            extends SideEffectOf<Removable>
            implements Removable
    {
        @This ParticipantState state;
        @This Participant participant;

        public boolean removeEntity()
        {
            if (result.removeEntity())
            {
                // Leave other groups and projects
                for (Group group : state.groups().toList())
                {
                    group.removeParticipant(participant);
                }

                for (Project project : state.projects().toList())
                {
                    project.removeMember(participant);
                }
            }

            return true;
        }

        public boolean reinstate()
        {
            return result.reinstate();
        }
    }

}
