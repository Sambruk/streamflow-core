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
package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Contains settings for priority
 */
@Mixins( PrioritySettings.Mixin.class )
public interface PrioritySettings
{
   void changeColor( @Optional String newColor );
   void changePriority( Integer newPriority );

   interface Data
   {
      @Optional
      Property<String> color();

      @Optional
      Property<Integer> priority();

      void changedColor( @Optional DomainEvent event, String newColor );
      void changedPriority( @Optional DomainEvent event, Integer newPriority );
   }


   abstract class Mixin
      implements PrioritySettings, Data
   {
      @This
      Data data;

      public void changeColor( @Optional String newColor )
      {
         if(((data.color().get() != null) && !data.color().get().equals( newColor ))
               || ((data.color().get() == null) && (newColor != null)))
         {
            data.changedColor( null, newColor );
         }
      }

      public void changePriority( Integer newPriority )
      {
         if( !newPriority.equals( data.priority().get() ))
            data.changedPriority( null, newPriority );
      }
   }
}
