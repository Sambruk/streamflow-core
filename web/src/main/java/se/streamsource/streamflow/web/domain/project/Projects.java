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

package se.streamsource.streamflow.web.domain.project;

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.organization.DuplicateDescriptionException;
import se.streamsource.streamflow.domain.project.ProjectStates;

/**
 * JAVADOC
 */
@Mixins(Projects.ProjectsMixin.class)
public interface Projects
{
    void addProject(SharedProject project) throws DuplicateDescriptionException;

    void removeProject(SharedProject project);

    void completeProject(SharedProject project);

    interface ProjectsState
    {
        @Aggregated
        ManyAssociation<SharedProject> projects();

        ManyAssociation<SharedProject> active();

        ManyAssociation<SharedProject> completed();

        ManyAssociation<SharedProject> dropped();
    }

    class ProjectsMixin
            implements Projects
    {
        @This
        ProjectsState state;

        public void addProject(SharedProject project) throws DuplicateDescriptionException
        {
            String groupName = project.getDescription();
            for (SharedProject aProject : state.projects())
            {
                if (aProject.hasDescription(groupName))
                {
                    throw new DuplicateDescriptionException();
                }
            }

            state.projects().add(state.projects().count(), project);
            state.active().add(state.active().count(), project);
        }

        public void removeProject(SharedProject project)
        {
            state.projects().remove(project);

            if (project.getStatus().equals(ProjectStates.ACTIVE))
            {
                state.active().remove(project);
            } else if (project.getStatus().equals(ProjectStates.COMPLETED))
            {
                state.completed().remove(project);
            } else if (project.getStatus().equals(ProjectStates.DROPPED))
            {
                state.dropped().remove(project);
            }
        }

        public void completeProject(SharedProject project)
        {
            if (project.complete())
            {
                state.active().remove(project);
                state.completed().add(state.completed().count(), project);
            }
        }
    }


}