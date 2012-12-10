/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.context.administration.surface.proxyusers;

import static se.streamsource.dci.api.RoleMap.role;

import java.io.IOException;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUsers;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

/**
 * JAVADOC
 */
public class ProxyUserContext
      implements IndexContext<StringValue>
{
   @Structure
   Module module;

   public void changeenabled(@Name("enabled") boolean enabled)
   {
      UserAuthentication userAuth = role( UserAuthentication.class );
      userAuth.changeEnabled( enabled );
   }

   public void resetpassword( @Name("password") String newPassword )
   {
      UserAuthentication authentication = role( UserAuthentication.class );

      authentication.resetPassword( newPassword );
   }

   public void delete() throws IOException
   {
      ProxyUsers.Data proxyUsers = role( ProxyUsers.Data.class );
      ProxyUser proxyUser = role( ProxyUser.class );
      UserAuthentication userAuth = role( UserAuthentication.class );
      UserAuthentication.Data userAuthData = role( UserAuthentication.Data.class );

      if (proxyUsers.proxyUsers().contains( proxyUser ))
      {
         proxyUsers.proxyUsers().remove( proxyUser );
      }
      //disable login for proxy user
      userAuth.changeEnabled( userAuthData.disabled().get() );
   }

   public StringValue index()
   {
      ProxyUser user = role( ProxyUser.class );

      ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );

      builder.prototype().string().set( user.getDescription() + " (" + ((UserAuthentication.Data) user).userName().get() + ')' );
      return builder.newInstance();
   }
}