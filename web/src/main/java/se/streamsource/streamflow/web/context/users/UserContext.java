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
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;
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
      extends Context
{
   @SubContext
   WorkspaceContext workspace();

   @SubContext
   OverviewContext overview();

   @SubContext
   UserAdministrationContext administration();

   void changepassword( ChangePasswordCommand newPassword ) throws WrongPasswordException;
   
   public void resetpassword( StringDTO command );
   public void changedisabled();

   abstract class Mixin
         extends ContextMixin
         implements UserContext
   {
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

      public UserAdministrationContext administration()
      {
         return subContext( UserAdministrationContext.class );
      }
   }
}
