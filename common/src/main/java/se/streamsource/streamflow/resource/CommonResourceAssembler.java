/**
 *
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.resource;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.dci.value.TitledLinksValue;
import se.streamsource.streamflow.infrastructure.application.AccessPointValue;
import se.streamsource.streamflow.infrastructure.application.GroupedListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.PageListItemValue;
import se.streamsource.streamflow.infrastructure.application.ResourceValue;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.resource.caze.CaseGeneralDTO;
import se.streamsource.streamflow.resource.caze.CaseValue;
import se.streamsource.streamflow.resource.caze.CasesQuery;
import se.streamsource.streamflow.resource.caze.ContactsDTO;
import se.streamsource.streamflow.resource.caze.EffectiveFieldDTO;
import se.streamsource.streamflow.resource.caze.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.caze.EndUserCaseDTO;
import se.streamsource.streamflow.resource.caze.FieldDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormListDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormsListDTO;
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
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;
import se.streamsource.streamflow.resource.user.NewProxyUserCommand;
import se.streamsource.streamflow.resource.user.NewUserCommand;
import se.streamsource.streamflow.resource.user.ProxyUserDTO;
import se.streamsource.streamflow.resource.user.ProxyUserListDTO;
import se.streamsource.streamflow.resource.user.RegisterUserCommand;
import se.streamsource.streamflow.resource.user.ResetPasswordCommand;
import se.streamsource.streamflow.resource.user.UserEntityDTO;
import se.streamsource.streamflow.resource.user.UserEntityListDTO;
import se.streamsource.streamflow.resource.user.profile.SearchValue;

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
            StringValue.class,
            DateDTO.class,
            IntegerDTO.class,
            NamedIndexDTO.class,
            EntityReferenceDTO.class,
            NewCommentCommand.class,
            NewUserCommand.class,
            NewProxyUserCommand.class,
            ResetPasswordCommand.class ).visibleIn( Visibility.application );

      // Queries
      module.addValues( CasesQuery.class ).visibleIn( Visibility.application );

      // Result values
      module.addValues(
            AccessPointValue.class,
            ResourceValue.class,
            LinkValue.class,
            LinksValue.class,
            TitledLinkValue.class,
            TitledLinksValue.class,
            ListValue.class,
            ListItemValue.class,
            GroupedListItemValue.class,
            PageListItemValue.class,
            CaseValue.class,
            CaseGeneralDTO.class,
            ContactsDTO.class,
            EndUserCaseDTO.class,
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
            UserEntityDTO.class,
            ProxyUserListDTO.class,
            ProxyUserDTO.class,
            SearchValue.class ).visibleIn( Visibility.application );
   }
}
