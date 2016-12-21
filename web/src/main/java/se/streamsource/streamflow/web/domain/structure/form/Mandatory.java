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
package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Mandatory.Mixin.class)
public interface Mandatory
{
   void changeMandatory( Boolean mandatory );

   boolean isMandatory();

   interface Data
   {
      @UseDefaults
      Property<Boolean> mandatory();

      void changedMandatory( @Optional DomainEvent event, Boolean mandatory );
   }

   class Mixin
      implements Mandatory
   {
      @This
      Data data;

      public void changeMandatory( Boolean mandatory )
      {
         if (mandatory.booleanValue() != data.mandatory().get().booleanValue())
         {
            data.changedMandatory( null, mandatory );
         }
      }

      public boolean isMandatory()
      {
         return data.mandatory().get();
      }
   }

}