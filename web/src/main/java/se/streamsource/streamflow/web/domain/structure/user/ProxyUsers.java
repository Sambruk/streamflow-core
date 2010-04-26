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

package se.streamsource.streamflow.web.domain.structure.user;

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(ProxyUsers.Mixin.class)
public interface ProxyUsers
{
   // Commands
   ProxyUser createProxyUser( String name, String username, String password );

   interface Data
   {
      @Aggregated
      ManyAssociation<ProxyUser> proxyUsers();

      ProxyUser createdProxyUser( DomainEvent event, String name, String username, String password );
   }

   abstract class Mixin
         implements ProxyUsers, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      public ProxyUser createProxyUser( String name, String username, String password )
      {
         return createdProxyUser( DomainEvent.CREATE, name, username, password );
      }

      public ProxyUser createdProxyUser( DomainEvent event, String name, String username, String password )
      {
         EntityBuilder<ProxyUser> builder = uowf.currentUnitOfWork().newEntityBuilder( ProxyUser.class );
         UserAuthentication.Data userEntity = builder.instanceFor( UserAuthentication.Data.class );
         userEntity.userName().set( username );
         userEntity.hashedPassword().set( userEntity.hashPassword( password ) );

         Describable.Data desc = builder.instanceFor( Describable.Data.class );
         desc.description().set( name );
         ProxyUser proxyUser = builder.newInstance();

         proxyUsers().add( proxyUser );

         return proxyUser;
      }
   }
}