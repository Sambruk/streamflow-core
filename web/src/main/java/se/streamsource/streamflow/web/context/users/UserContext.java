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

package se.streamsource.streamflow.web.context.users;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContext;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;
import se.streamsource.streamflow.web.context.users.overview.OverviewContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceContext;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.domain.structure.user.WrongPasswordException;

/**
 * JAVADOC
 */
@Mixins(UserContext.Mixin.class)
public interface UserContext extends Context, IndexContext<LinksValue>
{
   @SubContext
   WorkspaceContext workspace();

   @SubContext
   OverviewContext overview();

   @SubContext
   ContactableContext contact();

   @SubContext
   UserAdministrationContext administration();

   void changepassword(ChangePasswordCommand newPassword)
         throws WrongPasswordException;

   public void resetpassword(StringValue command);

   public void changedisabled();

   public void changemessagedeliverytype(StringValue command);

   public StringValue messagedeliverytype();

   abstract class Mixin extends ContextMixin implements UserContext
   {
      public LinksValue index()
      {
         return new LinksBuilder(module.valueBuilderFactory()).
               addLink("Drafts","drafts", "drafts", "workspace/user/drafts/cases", null).
               addLink("Inbox","inbox", "inbox", "workspace/user/inbox/cases", null).
               addLink("Assignments", "assignments", "assignments","workspace/user/assignments/cases", null).
               newLinks();
      }

      public void changepassword(ChangePasswordCommand newPassword)
            throws WrongPasswordException
      {
         UserAuthentication user = roleMap.get(UserAuthentication.class);

         user.changePassword(newPassword.oldPassword().get(), newPassword
               .newPassword().get());
      }

      public void resetpassword(StringValue command)
      {
         UserAuthentication user = roleMap.get(UserAuthentication.class);

         user.resetPassword(command.string().get());
      }

      public void changedisabled()
      {
         UserAuthentication user = roleMap.get(UserAuthentication.class);
         UserAuthentication.Data userData = roleMap
               .get(UserAuthentication.Data.class);

         user.changeEnabled(userData.disabled().get());
      }

      public void changemessagedeliverytype(StringValue newDeliveryType)
      {
         MessageRecipient recipient = roleMap.get(MessageRecipient.class);

         if (MessageRecipient.MessageDeliveryTypes.email.toString().equals(
               newDeliveryType.string().get()))
         {
            recipient
                  .changeMessageDeliveryType(MessageRecipient.MessageDeliveryTypes.email);
         } else
         {
            recipient
                  .changeMessageDeliveryType(MessageRecipient.MessageDeliveryTypes.none);
         }
      }

      public StringValue messagedeliverytype()
      {
         MessageRecipient.Data recipientData = roleMap
               .get(MessageRecipient.Data.class);

         ValueBuilder<StringValue> builder = module.valueBuilderFactory()
               .newValueBuilder(StringValue.class);
         builder.prototype().string().set(recipientData.delivery().toString());
         return builder.newInstance();
      }

      public WorkspaceContext workspace()
      {
         return subContext(WorkspaceContext.class);
      }

      public OverviewContext overview()
      {
         return subContext(OverviewContext.class);
      }

      public ContactableContext contact()
      {
         return subContext(ContactableContext.class);
      }

      public UserAdministrationContext administration()
      {
         return subContext(UserAdministrationContext.class);
      }
   }
}
