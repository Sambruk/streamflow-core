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

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.domain.roles.Removable;
import se.streamsource.streamflow.web.domain.DomainEntity;
import se.streamsource.streamflow.web.domain.label.Labels;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.task.Assignments;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.InboxQueries;
import se.streamsource.streamflow.web.domain.task.TaskId;
import se.streamsource.streamflow.web.domain.task.WaitingFor;
import se.streamsource.streamflow.web.domain.task.WaitingForQueries;

/**
 * JAVADOC
 */
@Mixins(ProjectEntity.ProjectIdGeneratorMixin.class)
public interface ProjectEntity
        extends DomainEntity,
        Project,

        // State
        InboxQueries,
        Inbox.InboxState,
        Assignments.AssignmentsState,
        WaitingForQueries,
        WaitingFor.WaitingForState,
        Members.MembersState,
        Describable.DescribableState,
        ProjectStatus.ProjectStatusState,
        ProjectOrganization.ProjectOrganizationState,
        Labels.LabelsState,
        ProjectFormDefinitions.ProjectFormDefinitionsState,
        ProjectFormDefinitionsQueries,
        Removable.RemovableState
{
    class ProjectIdGeneratorMixin
            implements IdGenerator
    {
        @This
        ProjectOrganization.ProjectOrganizationState state;

        public void assignId(TaskId task)
        {
            ((OrganizationalUnit.OrganizationalUnitState)state.organizationalUnit().get()).organization().get().assignId(task);
        }
    }
}
