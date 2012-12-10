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
package se.streamsource.streamflow.web.domain.structure.user;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.api.Password;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;

/**
 * JAVADOC
 */
@Mixins(ProxyUsers.Mixin.class)
public interface ProxyUsers
{
   // Commands

   ProxyUser createProxyUser( String name, @Password String password )
         throws IllegalArgumentException;

   interface Data
   {
      @Aggregated
      ManyAssociation<ProxyUser> proxyUsers();

      ProxyUser createdProxyUser( @Optional DomainEvent event, String id, String description, String password );
   }

   abstract class Mixin
         implements ProxyUsers, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

      @This
      Organization organization;

      public ProxyUser createProxyUser( String description, String password )
            throws IllegalArgumentException
      {
         return createdProxyUser( null, idGen.generate( Identity.class ), description, "#" + Strings.hashString( password ) );
      }

      public ProxyUser createdProxyUser( @Optional DomainEvent event, String id, String description, String password )
      {
         // check if the password is already hashed or not
         boolean isHashed = password.startsWith( "#" );
         EntityBuilder<ProxyUser> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( ProxyUser.class, id );
         builder.instance().organization().set( organization );
         UserAuthentication.Data userEntity = builder.instanceFor( UserAuthentication.Data.class );
         userEntity.userName().set( id );
         userEntity.hashedPassword().set( isHashed ? password.replace( "#", "" ) : Strings.hashString( password ) );

         Describable.Data desc = builder.instanceFor( Describable.Data.class );
         desc.description().set( description );
         ProxyUser proxyUser = builder.newInstance();

         proxyUsers().add( proxyUser );

         return proxyUser;
      }
   }
}