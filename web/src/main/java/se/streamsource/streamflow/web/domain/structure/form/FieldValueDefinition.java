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

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(FieldValueDefinition.Mixin.class)
public interface FieldValueDefinition
{
   void changeFieldValue( FieldValue fieldValue );

   interface Data
   {
      Property<FieldValue> fieldValue();

      void changedFieldValue( DomainEvent event, FieldValue fieldValue );
   }

   abstract class Mixin
         implements Data
   {
      public void changedFieldValue( DomainEvent event, FieldValue fieldValue )
      {
         fieldValue().set( fieldValue );
      }
   }
}