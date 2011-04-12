/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.api.*;
import se.streamsource.streamflow.resource.user.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.user.*;

import java.util.*;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class ProxyUsersContext
      implements IndexContext<ProxyUserListDTO>
{
   @Structure
   Module module;

   public void createproxyuser( NewProxyUserCommand proxyUser )
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