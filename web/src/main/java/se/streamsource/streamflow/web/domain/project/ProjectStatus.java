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

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.domain.project.ProjectStates;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * Status for a project. Possible transitions are:
 * Active -> Completed, Dropped
 * Completed -> Archived
 * Dropped -> Archived
 */
@Mixins(ProjectStatus.ProjectStatusMixin.class)
public interface ProjectStatus
{
    ProjectStates getStatus();
    void complete();
    void drop();
    void archive();

    interface ProjectStatusState
    {
        @UseDefaults
        Property<ProjectStates> status();

        void projectStatusChanged(DomainEvent event, ProjectStates newStatus);
    }

    abstract class ProjectStatusMixin
            implements ProjectStatus, ProjectStatusState
    {
        @This
        ProjectStatusState status;

        public ProjectStates getStatus()
        {
            return status.status().get();
        }

        public void complete()
        {
            if (status.status().get().equals(ProjectStates.ACTIVE))
            {
                projectStatusChanged(DomainEvent.CREATE, ProjectStates.COMPLETED);
            }
        }

        public void drop()
        {
            if (status.status().get().equals(ProjectStates.ACTIVE))
            {
                projectStatusChanged(DomainEvent.CREATE, ProjectStates.DROPPED);
            }
        }

        public void archive()
        {
            if (status.status().get().equals(ProjectStates.COMPLETED) || status.status().get().equals(ProjectStates.DROPPED))
            {
                projectStatusChanged(DomainEvent.CREATE, ProjectStates.ARCHIVED);
            } 
        }

        public void projectStatusChanged(DomainEvent event, ProjectStates newStatus)
        {
            status.status().set(newStatus);
        }
    }

}