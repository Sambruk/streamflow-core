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

package se.streamsource.streamflow.web.domain.structure.project;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@SideEffects(Members.RemovableSideEffect.class)
@Mixins(Members.Mixin.class)
public interface Members
{
   void addMember( Member member );

   void removeMember( Member member );

   void removeAllMembers();

   interface Data
   {
      ManyAssociation<Member> members();

      void addedMember( DomainEvent event, Member member );

      void removedMember( DomainEvent event, Member member );
   }

   abstract class Mixin
         implements Members, Data
   {
      @This
      Data state;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Project project;

      public void addMember( Member member )
      {
         if (members().contains( member ))
            return;

         addedMember( DomainEvent.CREATE, member );

         member.joinProject( project );
      }

      public void removeMember( Member member )
      {
         if (!members().contains( member ))
         {
            return;
         }
         member.leaveProject( project );

         removedMember( DomainEvent.CREATE, member );
      }

      public void removeAllMembers()
      {
         while (members().count() != 0)
         {
            removeMember( members().get( 0 ) );
         }
      }
   }

   abstract class RemovableSideEffect
         extends SideEffectOf<Removable>
         implements Removable
   {
      @This
      Members members;

      public boolean removeEntity()
      {
         if (result.removeEntity())
         {
            // Remove all project from the project
            members.removeAllMembers();
         }

         return true;
      }
   }
}
