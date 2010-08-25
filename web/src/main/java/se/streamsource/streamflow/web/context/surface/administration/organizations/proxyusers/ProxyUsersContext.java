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
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.streamflow.resource.user.NewProxyUserCommand;
import se.streamsource.streamflow.resource.user.ProxyUserDTO;
import se.streamsource.streamflow.resource.user.ProxyUserListDTO;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUsers;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(ProxyUsersContext.Mixin.class)
public interface ProxyUsersContext
   extends Context, IndexContext<ProxyUserListDTO>, SubContexts<ProxyUserContext>
{
   // commands
   void createproxyuser( NewProxyUserCommand proxyUser );

   abstract class Mixin
      extends ContextMixin
      implements ProxyUsersContext
   {
      public void createproxyuser( NewProxyUserCommand proxyUser )
      {
         Organization organization = roleMap.get( Organization.class );
         organization.createProxyUser( proxyUser.description().get(), proxyUser.password().get() );
      }

      public ProxyUserListDTO index()
      {
         ProxyUsers.Data data = roleMap.get( ProxyUsers.Data.class );

         ValueBuilder<ProxyUserListDTO> listBuilder = module.valueBuilderFactory().newValueBuilder( ProxyUserListDTO.class );

         ValueBuilder<ProxyUserDTO> builder = module.valueBuilderFactory().newValueBuilder( ProxyUserDTO.class );

         List<ProxyUser> proxyUsers = data.proxyUsers().toList();
         for( ProxyUser proxyUser : proxyUsers)
         {
            builder.prototype().username().set( ((UserAuthentication.Data)proxyUser).userName().get() );
            builder.prototype().disabled().set( ((UserAuthentication.Data)proxyUser).disabled().get() );
            builder.prototype().description().set( proxyUser.getDescription() );

            listBuilder.prototype().users().get().add( builder.newInstance() );
         }

         return listBuilder.newInstance();
      }

      public ProxyUserContext context( String id ) throws ContextNotFoundException
      {
         roleMap.set( module.unitOfWorkFactory().currentUnitOfWork().get( ProxyUser.class, id ));

         return subContext( ProxyUserContext.class );
      }
   }
}