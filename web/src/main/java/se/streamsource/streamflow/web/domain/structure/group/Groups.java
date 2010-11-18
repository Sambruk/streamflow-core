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

package se.streamsource.streamflow.web.domain.structure.group;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
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

   interface Data
   {
      @Aggregated
      ManyAssociation<Group> groups();

      Group createdGroup( @Optional DomainEvent event, String id );

      void addedGroup( @Optional DomainEvent event, Group group );

      void removedGroup( @Optional DomainEvent event, Group group );
   }

   abstract class Mixin
         implements Groups, Data
   {
      @This Data data;

      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      public Group createGroup( String name )
      {
         Group group = data.createdGroup(null, idGen.generate( GroupEntity.class ));
         group.changeDescription( name );
         addGroup(group);
         return group;
      }

      public void mergeGroups( Groups groups )
      {
         while (data.groups().count() > 0)
         {
            Group group = data.groups().get( 0 );
            data.removedGroup( null, group );
            groups.addGroup( group );
         }
      }

      public void addGroup( Group group )
      {
         if (!data.groups().contains( group ))
         {
            data.addedGroup( null, group );
         }
      }

      public boolean removeGroup( Group group )
      {
         if (!data.groups().contains( group ))
            return false;

         data.removedGroup( null, group );
         group.removeEntity();
         return true;
      }
   }
}
