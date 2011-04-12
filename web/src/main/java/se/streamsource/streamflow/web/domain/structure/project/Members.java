/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.qi4j.api.common.*;
import org.qi4j.api.entity.association.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.sideeffect.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.value.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.web.domain.structure.group.*;

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

   boolean isMember(Member member);

   interface Data
   {
      ManyAssociation<Member> members();

      void addedMember( @Optional DomainEvent event, Member member );

      void removedMember( @Optional DomainEvent event, Member member );
   }

   class Mixin
         implements Members
   {
      @This
      Data data;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Project project;

      public void addMember( Member member )
      {
         if (data.members().contains( member ))
            return;

         data.addedMember( null, member );

         member.joinProject( project );
      }

      public void removeMember( Member member )
      {
         if (!data.members().contains( member ))
         {
            return;
         }
         member.leaveProject( project );

         data.removedMember( null, member );
      }

      public void removeAllMembers()
      {
         while (data.members().count() != 0)
         {
            removeMember( data.members().get( 0 ) );
         }
      }

      public boolean isMember( Member member )
      {
         for (Member member1 : data.members())
         {
            if (member.equals(member1))
               return true;

            if (member instanceof Participants)
            {

            }
         }
         return false;
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
            // Remove all members from the project
            members.removeAllMembers();
         }

         return true;
      }
   }
}
