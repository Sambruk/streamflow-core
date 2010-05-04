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
@Mixins(EndUsers.Mixin.class)
public interface EndUsers
{
   AnonymousEndUser createAnonymousEndUser( String name );

   interface Data
   {
      @Aggregated
      ManyAssociation<AnonymousEndUser> anonymousEndUsers();

      AnonymousEndUser createdAnonymousEndUser( DomainEvent event, String name );
   }

   abstract class Mixin
         implements EndUsers, Data
   {
      @Structure
      UnitOfWorkFactory uowf;

      public AnonymousEndUser createAnonymousEndUser( String name )
      {
         return createdAnonymousEndUser( DomainEvent.CREATE, name );
      }

      public AnonymousEndUser createdAnonymousEndUser( DomainEvent event, String name )
      {
         EntityBuilder<AnonymousEndUser> builder = uowf.currentUnitOfWork().newEntityBuilder( AnonymousEndUser.class );
         Describable.Data desc = builder.instanceFor( Describable.Data.class );
         desc.description().set( name );

         AnonymousEndUser user = builder.newInstance();

         anonymousEndUsers().add( user );

         return user;
      }
   }
}