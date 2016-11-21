/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.structure.role;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.organization.RoleEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.ChangesOwner;

/**
 * JAVADOC
 */
@Mixins(Roles.Mixin.class)
public interface Roles
{
   Role createRole( String name);

   @ChangesOwner
   void addRole(Role role);

   void removeRole( Role projectRole );

   Role getAdministratorRole()
         throws IllegalStateException;

   interface Data
   {
      @Aggregated
      ManyAssociation<Role> roles();

      Role createdRole( @Optional DomainEvent event, String id );

      void addedRole( @Optional DomainEvent event, Role role );

      void removedRole( @Optional DomainEvent event, Role role );
   }

   public class Mixin
         implements Roles
   {
      @Service
      IdentityGenerator idGen;

      @This
      Data data;

      public Role createRole( String name )
      {
         Role role = data.createdRole( null, idGen.generate( RoleEntity.class ));
         data.addedRole( null, role );
         role.changeDescription( name );

         return role;
      }

      public void addRole( Role role )
      {
         if (!data.roles().contains( role ))
         {
            data.addedRole( null, role );
         }
      }

      public void removeRole( Role projectRole )
      {
         if (data.roles().contains( projectRole ))
         {
            data.removedRole(null, projectRole);
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