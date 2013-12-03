/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.context.account;

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.Role;
import se.streamsource.streamflow.api.Password;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.domain.structure.user.WrongPasswordException;
import se.streamsource.streamflow.web.infrastructure.plugin.ldap.LdapImporterService;

/**
 * JAVADOC
 */
@Mixins(AccountContext.Mixin.class)
public interface AccountContext
   extends TransientComposite
{
   public void changepassword(@Name("oldpassword") String oldPassword, @Name("newpassword") @Password String newPassword)
         throws WrongPasswordException;

   public String ldapon();

   abstract class Mixin
         implements AccountContext
   {
      @Structure
      Module module;

      AccountAdmin accountAdmin = new AccountAdmin();

      public void bind(@Uses UserAuthentication.Data user)
      {
         accountAdmin.bind(user);
      }

      public void changepassword(String oldPassword, String newPassword)
            throws WrongPasswordException
      {
         accountAdmin.changePassword(oldPassword, newPassword);
      }

      public String ldapon()
      {
         return "" + module.serviceFinder().findService( LdapImporterService.class ).isAvailable();
      }

      private class AccountAdmin
            extends Role<UserAuthentication.Data>
      {
         void changePassword(String oldPassword, String newPassword) throws WrongPasswordException
         {
            // Check if current password is correct
            if (!self.isCorrectPassword(oldPassword))
            {
               throw new WrongPasswordException();
            }

            self.changedPassword(null, Strings.hashString( newPassword ));
         }
      }
   }
}
