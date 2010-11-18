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

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(EndUsers.Mixin.class)
public interface EndUsers
{
   AnonymousEndUser createAnonymousEndUser( );

   boolean removeAnonymousEndUser( AnonymousEndUser user );

   void addAnonymousEndUser( AnonymousEndUser user);

   interface Data
   {
      @Aggregated
      ManyAssociation<AnonymousEndUser> anonymousEndUsers();

      AnonymousEndUser createdAnonymousEndUser( @Optional DomainEvent event, String id );

      void removedAnonymousEndUser( @Optional DomainEvent event, AnonymousEndUser user );

      void addedAnonymousEndUser( @Optional DomainEvent event, AnonymousEndUser user );
   }

   abstract class Mixin
         implements EndUsers, Data
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Service
      IdentityGenerator idgen;

      public AnonymousEndUser createAnonymousEndUser( )
      {
         String id = idgen.generate( Identity.class );

         AnonymousEndUser anonymousEndUser = createdAnonymousEndUser( null, id );
         addedAnonymousEndUser( null, anonymousEndUser );

         return anonymousEndUser;
      }

      public AnonymousEndUser createdAnonymousEndUser( DomainEvent event, String id )
      {
         EntityBuilder<AnonymousEndUser> builder = uowf.currentUnitOfWork().newEntityBuilder( AnonymousEndUser.class, id );
         return builder.newInstance();
      }

      public void addAnonymousEndUser( AnonymousEndUser user )
      {

         if (anonymousEndUsers().contains( user ))
         {
            return;
         }
         addedAnonymousEndUser( null, user );
      }

      public boolean removeAnonymousEndUser( AnonymousEndUser user )
      {
         if (!anonymousEndUsers().contains( user ))
            return false;

         removedAnonymousEndUser( null, user );
         user.removeEntity();
         return true;
      }
   }
}