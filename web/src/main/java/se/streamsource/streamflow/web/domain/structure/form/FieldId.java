/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.property.*;
import org.qi4j.library.constraints.annotation.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;

/**
 * Technical id for a field. May not be empty
 */
@Mixins(FieldId.Mixin.class)
public interface FieldId
{
   void changeFieldId( @NotEmpty String id );

   interface Data
   {
      @UseDefaults
      Property<String> fieldId();

      void changedFieldId( @Optional DomainEvent event, String newId );
   }

   class Mixin
      implements FieldId
   {
      @This
      Data data;

      public void changeFieldId( String id)
      {
         data.changedFieldId( null, id );
      }
   }

}