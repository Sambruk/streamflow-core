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

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.sideeffect.SideEffects;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.web.domain.label.Labels;
import se.streamsource.streamflow.web.domain.task.Assignments;
import se.streamsource.streamflow.web.domain.task.Delegatee;
import se.streamsource.streamflow.web.domain.task.Delegations;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.Owner;
import se.streamsource.streamflow.web.domain.task.WaitingFor;

/**
 * JAVADOC
 */
@SideEffects(AssignTaskIdSideEffect.class)
public interface ProjectEntity
        extends EntityComposite, 
        // Roles
        Project,
        Describable,
        Delegatee,
        Members,
        ProjectStatus,
        Inbox,
        Assignments,
        Delegations,
        WaitingFor,
        Owner,
        ProjectOrganization,

        // State
        Members.MembersState,
        Describable.DescribableState,
        ProjectStatus.ProjectStatusState,
        ProjectOrganization.ProjectOrganizationState

{
}
