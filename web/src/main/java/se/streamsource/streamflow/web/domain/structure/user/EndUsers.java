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
package se.streamsource.streamflow.web.domain.structure.user;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;

import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(EndUsers.Mixin.class)
public interface EndUsers
   extends EndUsersQueries
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
      Module module;

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
         return module.unitOfWorkFactory().currentUnitOfWork().get(EndUser.class, endUserId);
      }

      public EndUser createdEndUser( DomainEvent event, String id )
      {
         EntityBuilder<EndUser> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( EndUser.class, id );
         Contactable.Data contacts = builder.instanceFor( Contactable.Data.class );
         contacts.contact().set(module.valueBuilderFactory().newValue(ContactDTO.class));

         return builder.newInstance();
      }
   }
}