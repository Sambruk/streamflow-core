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
package se.streamsource.streamflow.web.context.administration.surface.proxyusers;

import static se.streamsource.dci.api.RoleMap.role;

import java.util.List;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.api.administration.NewProxyUserDTO;
import se.streamsource.streamflow.api.administration.ProxyUserDTO;
import se.streamsource.streamflow.api.administration.ProxyUserListDTO;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUsers;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

/**
 * JAVADOC
 */
public class ProxyUsersContext
      implements IndexContext<ProxyUserListDTO>
{
   @Structure
   Module module;

   public void createproxyuser( NewProxyUserDTO proxyUser )
   {
      Organization organization = role( Organization.class );
      organization.createProxyUser( proxyUser.description().get(), proxyUser.password().get() );
   }

   public ProxyUserListDTO index()
   {
      ProxyUsers.Data data = role( ProxyUsers.Data.class );

      ValueBuilder<ProxyUserListDTO> listBuilder = module.valueBuilderFactory().newValueBuilder( ProxyUserListDTO.class );

      ValueBuilder<ProxyUserDTO> builder = module.valueBuilderFactory().newValueBuilder( ProxyUserDTO.class );

      List<ProxyUser> proxyUsers = data.proxyUsers().toList();
      for (ProxyUser proxyUser : proxyUsers)
      {
         builder.prototype().username().set( ((UserAuthentication.Data) proxyUser).userName().get() );
         builder.prototype().disabled().set( ((UserAuthentication.Data) proxyUser).disabled().get() );
         builder.prototype().description().set( proxyUser.getDescription() );

         listBuilder.prototype().users().get().add( builder.newInstance() );
      }

      return listBuilder.newInstance();
   }
}