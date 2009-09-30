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

package se.streamsource.streamflow.resource;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.domain.user.UserSpecification;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.resource.assignment.AssignedTaskDTO;
import se.streamsource.streamflow.resource.assignment.AssignmentsTaskListDTO;
import se.streamsource.streamflow.resource.assignment.OverviewAssignedTaskDTO;
import se.streamsource.streamflow.resource.assignment.OverviewAssignmentsTaskListDTO;
import se.streamsource.streamflow.resource.comment.CommentDTO;
import se.streamsource.streamflow.resource.comment.CommentsDTO;
import se.streamsource.streamflow.resource.comment.NewCommentCommand;
import se.streamsource.streamflow.resource.delegation.DelegatedTaskDTO;
import se.streamsource.streamflow.resource.delegation.DelegationsTaskListDTO;
import se.streamsource.streamflow.resource.inbox.InboxTaskDTO;
import se.streamsource.streamflow.resource.inbox.InboxTaskListDTO;
import se.streamsource.streamflow.resource.organization.MergeOrganizationalUnitCommand;
import se.streamsource.streamflow.resource.organization.MoveOrganizationalUnitCommand;
import se.streamsource.streamflow.resource.organization.search.SearchTaskDTO;
import se.streamsource.streamflow.resource.organization.search.SearchTaskListDTO;
import se.streamsource.streamflow.resource.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.resource.overview.ProjectSummaryListDTO;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.task.TaskContactsDTO;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;
import se.streamsource.streamflow.resource.user.RegisterUserCommand;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskDTO;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskListDTO;

/**
 * JAVADOC
 */
public class CommonResourceAssembler
        implements Assembler
{
    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        // Commands
        module.addValues(RegisterUserCommand.class,
                ChangePasswordCommand.class,
                StringDTO.class,
                DateDTO.class,
                EntityReferenceDTO.class,
                NewCommentCommand.class,
                MoveOrganizationalUnitCommand.class,
                MergeOrganizationalUnitCommand.class).visibleIn(Visibility.application);

        // Queries
        module.addValues(UserSpecification.class, TasksQuery.class).visibleIn(Visibility.application);

        // Result values
        module.addValues(ListValue.class, ListItemValue.class,
                InboxTaskListDTO.class,
                InboxTaskDTO.class,
                TaskGeneralDTO.class,
                TaskContactsDTO.class,
                CommentsDTO.class,
                CommentDTO.class,
                AssignmentsTaskListDTO.class,
                AssignedTaskDTO.class,
                DelegationsTaskListDTO.class,
                DelegatedTaskDTO.class,
                WaitingForTaskListDTO.class,
                WaitingForTaskDTO.class,
                TreeValue.class,
                TreeNodeValue.class,
                OverviewAssignmentsTaskListDTO.class,
                OverviewAssignedTaskDTO.class,
                ProjectSummaryDTO.class,
                ProjectSummaryListDTO.class).visibleIn(Visibility.application);

        module.addValues(SearchTaskListDTO.class,
                SearchTaskDTO.class).visibleIn(Visibility.application);
    }
}
