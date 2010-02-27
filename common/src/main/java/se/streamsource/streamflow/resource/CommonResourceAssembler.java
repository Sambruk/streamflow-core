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
import se.streamsource.streamflow.infrastructure.application.GroupedListItemValue;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.PageListItemValue;
import se.streamsource.streamflow.infrastructure.application.ResourceValue;
import se.streamsource.streamflow.infrastructure.application.TitledLinkValue;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.resource.comment.CommentDTO;
import se.streamsource.streamflow.resource.comment.CommentsDTO;
import se.streamsource.streamflow.resource.comment.NewCommentCommand;
import se.streamsource.streamflow.resource.conversation.ConversationDTO;
import se.streamsource.streamflow.resource.conversation.MessageDTO;
import se.streamsource.streamflow.resource.overview.ProjectSummaryDTO;
import se.streamsource.streamflow.resource.overview.ProjectSummaryListDTO;
import se.streamsource.streamflow.resource.roles.BooleanDTO;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.roles.NamedIndexDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.task.EffectiveFieldDTO;
import se.streamsource.streamflow.resource.task.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.task.FieldDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormListDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormsListDTO;
import se.streamsource.streamflow.resource.task.TaskContactsDTO;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;
import se.streamsource.streamflow.resource.task.TaskValue;
import se.streamsource.streamflow.resource.task.TasksQuery;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;
import se.streamsource.streamflow.resource.user.NewUserCommand;
import se.streamsource.streamflow.resource.user.RegisterUserCommand;
import se.streamsource.streamflow.resource.user.ResetPasswordCommand;
import se.streamsource.streamflow.resource.user.UserEntityDTO;
import se.streamsource.streamflow.resource.user.UserEntityListDTO;

/**
 * JAVADOC
 */
public class CommonResourceAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      // Commands
      module.addValues( RegisterUserCommand.class,
            ChangePasswordCommand.class,
            BooleanDTO.class,
            StringDTO.class,
            DateDTO.class,
            IntegerDTO.class,
            NamedIndexDTO.class,
            EntityReferenceDTO.class,
            NewCommentCommand.class,
            NewUserCommand.class,
            ResetPasswordCommand.class ).visibleIn( Visibility.application );

      // Queries
      module.addValues( TasksQuery.class ).visibleIn( Visibility.application );

      // Result values
      module.addValues(
            ResourceValue.class,
            LinkValue.class,
            LinksValue.class,
            TitledLinkValue.class,
            ListValue.class,
            ListItemValue.class,
            GroupedListItemValue.class,
            PageListItemValue.class,
            TaskValue.class,
            TaskGeneralDTO.class,
            TaskContactsDTO.class,
            CommentsDTO.class,
            CommentDTO.class,
            ConversationDTO.class,
            MessageDTO.class,
            TreeValue.class,
            TreeNodeValue.class,
            ProjectSummaryDTO.class,
            ProjectSummaryListDTO.class,
            FieldDTO.class,
            EffectiveFieldsDTO.class,
            EffectiveFieldDTO.class,
            SubmittedFormDTO.class,
            SubmittedFormListDTO.class,
            SubmittedFormsListDTO.class,
            UserEntityListDTO.class,
            UserEntityDTO.class ).visibleIn( Visibility.application );
   }
}
