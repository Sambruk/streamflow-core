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

package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.role.Roles;

/**
 * JAVADOC
 */
@Mixins(OrganizationalUnits.Mixin.class)
public interface OrganizationalUnits
{
   OrganizationalUnit createOrganizationalUnit( String name );

   void addOrganizationalUnit( OrganizationalUnit ou );

   void removeOrganizationalUnit( OrganizationalUnit ou );

   interface Data
   {
      @Aggregated
      ManyAssociation<OrganizationalUnit> organizationalUnits();

      OrganizationalUnit createdOrganizationalUnit( DomainEvent event, @Name("id") String id );

      void removedOrganizationalUnit( DomainEvent create, OrganizationalUnit ou );

      void addedOrganizationalUnit( DomainEvent event, OrganizationalUnit ou );

      OrganizationalUnits getParent( OrganizationalUnit ou );

   }

   abstract class Mixin
         implements OrganizationalUnits, Data
   {
      @Service
      IdentityGenerator idGenerator;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      RolePolicy policy;

      @This
      Roles.Data roles;

      @This
      OwningOrganization orgOwner;

      @This
      OrganizationalUnits organizationalUnits;

      public OrganizationalUnit createOrganizationalUnit( String name )
      {
         OrganizationalUnit ou = createdOrganizationalUnit( DomainEvent.CREATE, idGenerator.generate( Identity.class ) );
         addOrganizationalUnit( ou );
         ou.changeDescription( name );

         // Add current user as administrator
         ou.grantAdministratorToCurrentUser();

         return ou;
      }

      public void addOrganizationalUnit( OrganizationalUnit ou )
      {
         if (organizationalUnits().contains( ou ))
         {
            return;
         }
         addedOrganizationalUnit( DomainEvent.CREATE, ou );
      }

      public void removeOrganizationalUnit( OrganizationalUnit ou )
      {
         if (!organizationalUnits().contains( ou ))
            return; // OU is not a sub-OU of this OU

         removedOrganizationalUnit( DomainEvent.CREATE, ou );
      }

      public OrganizationalUnit createdOrganizationalUnit( DomainEvent event, @Name("id") String id )
      {
         EntityBuilder<OrganizationalUnit> ouBuilder = uowf.currentUnitOfWork().newEntityBuilder( OrganizationalUnit.class, id );
         ouBuilder.instanceFor(OwningOrganization.class).organization().set( orgOwner.organization().get() );
         OrganizationalUnit ou = ouBuilder.newInstance();
         return ou;
      }

      public void removedOrganizationalUnit( DomainEvent create, OrganizationalUnit ou )
      {
         organizationalUnits().remove( ou );
      }

      public void addedOrganizationalUnit( DomainEvent event, OrganizationalUnit ou )
      {
         organizationalUnits().add( organizationalUnits().count(), ou );
      }


      public OrganizationalUnits getParent( OrganizationalUnit ou )
      {
         if (organizationalUnits().contains( ou ))
         {
            return organizationalUnits;
         } else
         {
            for (OrganizationalUnit organizationalUnit : organizationalUnits())
            {
               Data state = (Data) organizationalUnit;
               OrganizationalUnits parent = state.getParent( ou );
               if (parent != null)
               {
                  return parent;
               }
            }
         }
         return null;
      }
   }
}
