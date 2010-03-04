/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.context.users;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;
import se.streamsource.streamflow.web.context.task.ContactContext;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.domain.structure.user.WrongPasswordException;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.SubContext;
import se.streamsource.streamflow.web.context.users.overview.OverviewContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceContext;

/**
 * JAVADOC
 */
@Mixins(UserContext.Mixin.class)
public interface UserContext
      extends Context, IndexContext<LinksValue>
{
   @SubContext
   WorkspaceContext workspace();

   @SubContext
   OverviewContext overview();

   @SubContext
   ContactableContext contact();

   @SubContext
   UserAdministrationContext administration();

   void changepassword( ChangePasswordCommand newPassword ) throws WrongPasswordException;
   
   public void resetpassword( StringDTO command );
   public void changedisabled();

   abstract class Mixin
         extends ContextMixin
         implements UserContext
   {
      public LinksValue index()
      {
         return new LinksBuilder( module.valueBuilderFactory()).
               addLink( "Inbox", "inbox", "inbox", "workspace/user/inbox/tasks" ).
               addLink( "Assignments", "assignments", "assignments", "workspace/user/assignments/tasks" ).
               addLink( "Delegations", "delegations", "delegations", "workspace/user/delegations/tasks" ).
               addLink( "Waiting for", "waitingfor", "waitingfor", "workspace/user/waitingfor/tasks" ).
               newLinks();
      }

      public void changepassword( ChangePasswordCommand newPassword ) throws WrongPasswordException
      {
         UserAuthentication user = context.role(UserAuthentication.class);

         user.changePassword( newPassword.oldPassword().get(), newPassword.newPassword().get() );
      }

      public void resetpassword( StringDTO command )
      {
         UserAuthentication user = context.role(UserAuthentication.class);

         user.resetPassword( command.string().get() );
      }

      public void changedisabled()
      {
         UserAuthentication user = context.role(UserAuthentication.class);
         UserAuthentication.Data userData = context.role(UserAuthentication.Data.class);

         user.changeEnabled( userData.disabled().get() );
      }

      public WorkspaceContext workspace()
      {
         return subContext( WorkspaceContext.class );
      }

      public OverviewContext overview()
      {
         return subContext( OverviewContext.class );
      }

      public ContactableContext contact()
      {
         return subContext( ContactableContext.class );
      }

      public UserAdministrationContext administration()
      {
         return subContext( UserAdministrationContext.class );
      }
   }
}
