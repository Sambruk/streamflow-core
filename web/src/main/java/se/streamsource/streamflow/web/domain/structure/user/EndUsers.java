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
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.domain.contact.Contactable;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(EndUsers.Mixin.class)
public interface EndUsers
{
   EndUser createEndUser(String id );

   EndUser getEndUser(String id)
           throws NoSuchEntityException;

   interface Data
   {
      EndUser createdEndUser( @Optional DomainEvent event, String id );
   }

   abstract class Mixin
         implements EndUsers, Data
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @This
      Identity identity;

      public EndUser createEndUser(String id )
      {
         String endUserId = identity.identity().get()+"/"+id;

         EndUser endUser = createdEndUser(null, endUserId);

         return endUser;
      }

      public EndUser getEndUser(String id)
              throws NoSuchEntityException
      {
         // End-user id == <proxyuser-id>/<given id>
         String endUserId = identity.identity().get()+"/"+id;
         return uowf.currentUnitOfWork().get(EndUser.class, endUserId);
      }

      public EndUser createdEndUser( DomainEvent event, String id )
      {
         EntityBuilder<EndUser> builder = uowf.currentUnitOfWork().newEntityBuilder( EndUser.class, id );
         Contactable.Data contacts = builder.instanceFor( Contactable.Data.class );
         contacts.contact().set( vbf.newValue( ContactValue.class ) );

         return builder.newInstance();
      }
   }
}