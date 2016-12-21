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
      Association<Priority> casepriority();
      void changedPriority( @Optional DomainEvent event, @Optional Priority priority );
   }

   abstract class Mixin
      implements CasePriority, Data
   {
      @This
      Data data;
      public void changePriority( @Optional Priority priority )
      {
         // check if there would actually be a change before changing
         if( ( data.casepriority().get() == null && priority == null) ||
               ( priority != null && priority.equals( data.casepriority().get() )))
            return;
         
         data.changedPriority( null, priority );
      }

      public void changedPriority( DomainEvent event, Priority priority )
      {
         data.casepriority().set(priority);
      }
   }
}
