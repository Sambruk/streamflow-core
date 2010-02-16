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

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Notable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(FieldTemplate.Mixin.class)
public interface FieldTemplate
      extends Describable, Notable, FieldValueDefinition
{
   void changeMandatory( Boolean mandatory );

   Boolean getMandatory();

   interface Data
   {
      @UseDefaults
      Property<Boolean> mandatory();

      void changedMandatory( DomainEvent event, Boolean mandatory );
   }

   abstract class Mixin
      implements FieldTemplate, Data
   {
      public void changeMandatory( Boolean mandatory )
      {
         if (mandatory.booleanValue() != mandatory().get().booleanValue())
         {
            changedMandatory( DomainEvent.CREATE, mandatory );
         }
      }

      public void changedMandatory( DomainEvent event, Boolean mandatory )
      {
         mandatory().set( mandatory );
      }

      public Boolean getMandatory()
      {
         return mandatory().get();
      }
   }

}