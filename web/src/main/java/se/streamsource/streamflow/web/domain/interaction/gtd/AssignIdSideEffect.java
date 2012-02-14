/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.sideeffect.SideEffectOf;

/**
 * Assign id to case if sent to project inbox
 */
public class AssignIdSideEffect
      extends SideEffectOf<Ownable>
      implements Ownable
{
   @This
   CaseId id;

   public boolean isOwnedBy( Owner owner )
   {
      return result.isOwnedBy( owner );
   }

   public boolean hasOwner()
   {
      return result.hasOwner();
   }

   public void changeOwner( Owner owner )
   {
      result.changeOwner( owner );

      if (owner instanceof IdGenerator)
      {
         IdGenerator idgen = (IdGenerator) owner;
         id.assignId( idgen );
      }
   }
}
