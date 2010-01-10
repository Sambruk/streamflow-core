/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.structure.group;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Concerns(Groups.DescribeCreatedGroupConcern.class)
@Mixins(Groups.Mixin.class)
public interface Groups
{
   Group createGroup( String name );

   void addGroup( Group group );

   boolean removeGroup( Group group );

   void mergeGroups( Groups groups );

   interface Data
   {
      @Aggregated
      ManyAssociation<Group> groups();

      Group createdGroup( DomainEvent event, String id );

      void addedGroup( DomainEvent event, Group group );

      void removedGroup( DomainEvent event, Group group );

      Group getGroupByName( String name );
   }

   abstract class Mixin
         implements Groups, Data
   {
      public void mergeGroups( Groups groups )
      {

         while (this.groups().count() > 0)
         {
            Group group = this.groups().get( 0 );
            removedGroup( DomainEvent.CREATE, group );
            groups.addGroup( group );
         }

      }

      public void addGroup( Group group )
      {
         if (groups().contains( group ))
         {
            return;
         }
         addedGroup( DomainEvent.CREATE, group );
      }

      public Group getGroupByName( String name )
      {
         return Describable.Mixin.getDescribable( groups(), name );
      }
   }

   abstract class DescribeCreatedGroupConcern
         extends ConcernOf<Groups>
         implements Groups
   {
      public Group createGroup( String name )
      {
         Group group = next.createGroup( name );
         group.changeDescription( name );
         return group;
      }
   }

}
