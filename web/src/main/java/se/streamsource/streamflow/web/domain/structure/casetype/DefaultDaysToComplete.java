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
package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.Range;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * TODO
 */
@Mixins(DefaultDaysToComplete.Mixin.class)
public interface DefaultDaysToComplete
{
   void changeDefaultDaysToComplete(@Range(min=0, max=Double.MAX_VALUE) Integer defaultDaysToComplete);

   interface Data
   {
      @UseDefaults
      Property<Integer> defaultDaysToComplete();
   }

   interface Events
   {
      void changedDefaultDaysToComplete(@Optional DomainEvent event, int defaultDaysToComplete);
   }

   class Mixin
      implements DefaultDaysToComplete, Events
   {
      @This Data data;

      public void changeDefaultDaysToComplete(Integer defaultDaysToComplete)
      {
         changedDefaultDaysToComplete(null, defaultDaysToComplete);
      }

      public void changedDefaultDaysToComplete(@Optional DomainEvent event, int defaultDaysToComplete)
      {
         data.defaultDaysToComplete().set(defaultDaysToComplete);
      }
   }
}
