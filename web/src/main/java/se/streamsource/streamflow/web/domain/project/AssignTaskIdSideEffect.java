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

package se.streamsource.streamflow.web.domain.project;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.sideeffect.SideEffectOf;
import se.streamsource.streamflow.web.domain.task.Ownable;
import se.streamsource.streamflow.web.domain.task.Owner;
import se.streamsource.streamflow.web.domain.task.TaskId;

/**
 * Assign id to task if sent to project inbox
 */
public class AssignTaskIdSideEffect
      extends SideEffectOf<Ownable>
      implements Ownable
{
   @This
   TaskId id;

   public boolean isOwnedBy( Owner owner )
   {
      return result.isOwnedBy( owner );
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
