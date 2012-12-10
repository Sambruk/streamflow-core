/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.domain.structure.project;

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
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Participant;

/**
 * JAVADOC
 */
@SideEffects(Memberships.RemovableSideEffect.class)
@Mixins(Memberships.Mixin.class)
public interface Memberships
{
   void joinProject( Project project );

   void leaveProject( Project project );

   boolean isMember(Project project);

   interface Data
   {
      ManyAssociation<Project> projects();

      void joinedProject( @Optional DomainEvent event, Project project);

      void leftProject( @Optional DomainEvent event, Project project );
   }

   abstract class Mixin
         implements Memberships, Data
   {
      @Structure
      Module module;

      @This
      Participant participant;

      @This
      Data state;

      public void joinProject( Project project )
      {
         if (state.projects().contains( project ))
            return;

         joinedProject( null, project );
      }

      public void leaveProject( Project project )
      {
         if (!state.projects().contains( project ))
            return;

         leftProject( null, project );
      }

      public boolean isMember( Project project )
      {
         if (projects().contains( project ))
            return true;

         // Check groups
         for (Group group : participant.allGroups())
         {
            if (((Member)group).isMember( project ))
               return true;
         }

         return false;
      }

      public void joinedProject( DomainEvent event, Project project )
      {
         state.projects().add( project );
      }

      public void leftProject( DomainEvent event, Project project )
      {
         state.projects().remove( project );
      }
   }

   class RemovableSideEffect
         extends SideEffectOf<Removable>
         implements Removable
   {
      @This
      Memberships.Data state;

      @This
      Member member;

      public boolean removeEntity()
      {
         if (result.removeEntity())
         {
            // Leave project
            for (Members members : state.projects().toList())
            {
               members.removeMember( member );
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