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
package se.streamsource.streamflow.web.domain.structure.caze;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.organization.Priority;

/**
 * Contains case priority information for a case.
 */
@Mixins(CasePriority.Mixin.class)
public interface CasePriority
{
   void changePriority( @Optional Priority priority );
   interface Data
   {
      @Optional
      Association<Priority> priority();
   }
   
   interface Events
   {
      void changedPriority( @Optional DomainEvent event, @Optional Priority priority );
   }

   abstract class Mixin
      implements CasePriority, Events
   {
      @This
      Data data;
      public void changePriority( @Optional Priority priority )
      {
         // check if there would actually be a change before changing
         if( ( data.priority().get() == null && priority == null) ||
               ( priority != null && priority.equals( data.priority().get() )))
            return;
         
         changedPriority( null, priority );
      }
   }
}
