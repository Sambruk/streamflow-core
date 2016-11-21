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
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Represents a subcase. Keeps track of the parent of the subcase, which can be null.
 */
@Mixins(SubCase.Mixin.class)
public interface SubCase
{
   void changeParent( @Optional Case newParent );

   interface Data
   {
      @Optional
      Association<Case> parent();

      void changedParent( @Optional DomainEvent event, @Optional Case newParent );
   }

   abstract class Mixin
         implements SubCase, Data
   {
      public void changeParent( Case newParent )
      {
         if (newParent == null || !newParent.equals( parent().get() ))
         {
            changedParent( null, newParent );
         }
      }

      public void changedParent( @Optional DomainEvent event, Case newParent )
      {
         parent().set( newParent );
      }
   }
}
