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

package se.streamsource.streamflow.web.context.surface.administration.organizations.proxyusers;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.domain.structure.user.WrongPasswordException;

/**
 * JAVADOC
 */
@Mixins(ProxyUserContext.Mixin.class)
public interface ProxyUserContext
   extends Interactions, DeleteInteraction, IndexInteraction<StringValue>
{
   void resetpassword( StringValue newPassword );

   void changepassword( ChangePasswordCommand command ) throws WrongPasswordException;

   abstract class Mixin
      extends InteractionsMixin
      implements ProxyUserContext
   {
      public void changepassword( ChangePasswordCommand command ) throws WrongPasswordException
      {
         UserAuthentication authentication = context.get( UserAuthentication.class );

         authentication.changePassword( command.oldPassword().get(), command.newPassword().get() );
      }

      public void resetpassword( StringValue newPassword )
      {
         UserAuthentication authentication = context.get( UserAuthentication.class );

         authentication.resetPassword( newPassword.toString() );
      }

      public void delete() throws ResourceException
      {
         // TODO
      }

      public StringValue index()
      {
         ProxyUser user = context.get( ProxyUser.class );

         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );

         builder.prototype().string().set( user.getDescription() + " (" + ((UserAuthentication.Data)user).userName().get()+')');
         return builder.newInstance();
      }
   }
}