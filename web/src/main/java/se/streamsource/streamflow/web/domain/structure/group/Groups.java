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
package se.streamsource.streamflow.web.domain.structure.group;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.ExternalReference;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.ChangesOwner;

/**
 * JAVADOC
 */
@Mixins(Groups.Mixin.class)
public interface Groups
{
   Group createGroup( String name );

   @ChangesOwner
   void addGroup( Group group );

   boolean removeGroup( Group group );

   void mergeGroups( Groups groups );

   Group findByExternalReference( String externalReference );

   interface Data
   {
      @Aggregated
      ManyAssociation<Group> groups();
   }

   interface Events
   {
      Group createdGroup( @Optional DomainEvent event, String id );

      void addedGroup( @Optional DomainEvent event, Group group );

      void removedGroup( @Optional DomainEvent event, Group group );
   }

   class Mixin
         implements Groups, Events
   {
      @This Data data;

      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

      public Group createGroup( String name )
      {
         Group group = createdGroup(null, idGen.generate(GroupEntity.class));
         group.changeDescription( name );
         addGroup(group);
         return group;
      }

      public void mergeGroups( Groups groups )
      {
         for (Group group : data.groups().toList())
         {
            removedGroup(null, group);
            groups.addGroup( group );
         }
      }

      public void addGroup( Group group )
      {
         if (!data.groups().contains( group ))
         {
            addedGroup(null, group);
         }
      }

      public boolean removeGroup( Group group )
      {
         if (!data.groups().contains( group ))
            return false;

         removedGroup(null, group);
         group.removeEntity();
         return true;
      }

      public Group createdGroup(@Optional DomainEvent event, String id)
      {
         return module.unitOfWorkFactory().currentUnitOfWork().newEntity(GroupEntity.class, id);
      }

      public void addedGroup(@Optional DomainEvent event, Group group)
      {
         data.groups().add(group);
      }

      public void removedGroup(@Optional DomainEvent event, Group group)
      {
         data.groups().remove(group);
      }

      public Group findByExternalReference( final String externalReference )
      {
         return Iterables.first( Iterables.filter( new Specification<Group>()
         {
            public boolean satisfiedBy( Group item )
            {
               boolean hasValue = !Strings.empty( ((ExternalReference.Data)item).reference().get() );
               return  hasValue ? ((ExternalReference.Data)item).reference().get().equals( externalReference ) : hasValue;
            }
         }, data.groups() ) );
      }
   }
}
