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
package se.streamsource.streamflow.web.domain.structure.group;

import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.Removable;

/**
 * JAVADOC
 */
@SideEffects(Participation.RemovableSideEffect.class)
@Mixins(Participation.Mixin.class)
public interface Participation
{
   void joinGroup( Group participants );

   void leaveGroup( Group participants );

   /**
    * Return all groups that this participant is a member of, transitively.
    *
    * @return all groups that this participant is a member of
    */
   Iterable<Group> allGroups();

   interface Data
   {
      ManyAssociation<Group> groups();

      void joinedGroup( @Optional DomainEvent event, Group group );

      void leftGroup( @Optional DomainEvent event, Group group );
   }

   abstract class Mixin
         implements Participation, Data
   {
      @Structure
      Module module;

      @This
      Participation participant;

      @This
      Data state;

      public void joinGroup( Group group )
      {
         if (!group.equals( participant ))
            joinedGroup( null, group );
      }

      public void leaveGroup( Group group )
      {
         if (!state.groups().contains( group ))
            return;

         leftGroup( null, group );
      }

      public Iterable<Group> allGroups()
      {
         List<Group> groups = new ArrayList<Group>();
         for (Group group : state.groups())
         {
            if (!groups.contains( group ))
               groups.add( group );

            // Add transitively
            Participation participation = (Participation) group;
            for (Participants group1 : participation.allGroups())
            {
               if (!groups.contains( group1 ))
                  groups.add( group );
            }
         }

         return groups;
      }

      public void joinedGroup( @Optional DomainEvent event, Group group )
      {
         state.groups().add( group );
      }

      public void leftGroup( @Optional DomainEvent event, Group group )
      {
         state.groups().remove( group );
      }
   }

   class RemovableSideEffect
         extends SideEffectOf<Removable>
         implements Removable
   {
      @This
      Participation.Data state;

      @This
      Participant participant;

      public boolean removeEntity()
      {
         if (result.removeEntity())
         {
            // Leave other groups and projects
            for (Participants group : state.groups().toList())
            {
               group.removeParticipant( participant );
            }
         }

         return true;
      }

      public boolean reinstate()
      {
         return result.reinstate();
      }

      public void deleteEntity()
      {
         result.deleteEntity();
      }
   }
}