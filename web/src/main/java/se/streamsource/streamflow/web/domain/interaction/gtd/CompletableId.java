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

package se.streamsource.streamflow.web.domain.interaction.gtd;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.Matches;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * Human readable id
 */
@Mixins(CompletableId.Mixin.class)
public interface CompletableId
{
   /**
    * Set new id for the task. It needs to be on the format:
    * yyyymmdd-n
    * such as:
    * 20090320-123
    *
    * @param id
    */
   void assignId( @Matches("\\d{8}-\\d*") String id );

   void assignId( IdGenerator idgen );

   interface Data
   {
      @Optional
      Property<String> taskId();


      void assignedTaskId( DomainEvent event, String id );
   }

   abstract class Mixin
         implements CompletableId, Data
   {
      @This
      Data state;

      @This
      CompletableId id;

      public void assignId( IdGenerator idgen )
      {
         if (state.taskId().get() == null)
         {
            idgen.assignId( id );
         }
      }

      public void assignId( String id )
      {
         if (state.taskId().get() == null)
         {
            state.assignedTaskId( DomainEvent.CREATE, id );
         }
      }

      // Event

      public void assignedTaskId( DomainEvent event, String id )
      {
         state.taskId().set( id );
      }
   }
}
