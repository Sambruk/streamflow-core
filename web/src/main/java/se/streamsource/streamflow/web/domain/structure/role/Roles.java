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

package se.streamsource.streamflow.web.domain.structure.role;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.organization.RoleEntity;

/**
 * JAVADOC
 */
@Mixins(Roles.Mixin.class)
public interface Roles
{
   Role createRole( String name);

   void removeRole( Role projectRole );

   Role getAdministratorRole()
         throws IllegalStateException;

   interface Data
   {
      @Aggregated
      ManyAssociation<Role> roles();

      Role createdRole( DomainEvent event, String id );

      void addedRole( DomainEvent event, Role role );

      void removedRole( DomainEvent event, Role role );
   }

   public class Mixin
         implements Roles
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Data data;

      public Role createRole( String name )
      {
         Role role = data.createdRole( DomainEvent.CREATE, idGen.generate( RoleEntity.class ));
         data.addedRole( DomainEvent.CREATE, role );
         role.changeDescription( name );

         return role;
      }

      public void removeRole( Role projectRole )
      {
         if (data.roles().contains( projectRole ))
         {
            data.removedRole(DomainEvent.CREATE, projectRole);
            projectRole.removeEntity();
         }
      }

      public Role getAdministratorRole()
            throws IllegalStateException
      {
         if (data.roles().count() > 0)
            return data.roles().get( 0 );
         else
            throw new IllegalStateException( "There's no Administrator role!" );
      }
   }
}