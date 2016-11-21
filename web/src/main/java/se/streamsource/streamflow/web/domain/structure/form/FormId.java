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
import org.qi4j.library.constraints.annotation.NotEmpty;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * Technical id for a form. May not be empty.
 */
@Mixins(FormId.Mixin.class)
public interface FormId
{
   void changeFormId( @NotEmpty String id );

   interface Data
   {
      @UseDefaults
      Property<String> formId();

      void changedFormId( @Optional DomainEvent event, String newId );
   }

   class Mixin
      implements FormId
   {
      @This
      Data data;

      public void changeFormId( String id)
      {
         data.changedFormId( null, id );
      }
   }

}