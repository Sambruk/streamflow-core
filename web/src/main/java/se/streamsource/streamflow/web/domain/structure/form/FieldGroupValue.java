/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(FieldGroupValue.Mixin.class)
public interface FieldGroupValue
{
   void addFieldGroup( Field field );
   void addFieldGroupField( Field field );

   Field getFieldGroupField();

   interface Data
   {
      // points to the field which is the field group
      Association<Field> fieldGroup();

      // points to the field contained within the field group
      Association<Field> fieldGroupField();

      void fieldGroupAdded( @Optional DomainEvent event, Field field );

      void fieldGroupFieldAdded( @Optional DomainEvent event, Field field );
   }

   abstract class Mixin
         implements Data, FieldGroupValue
   {

      @This
      Data data;

      public void addFieldGroup( Field field )
      {
         fieldGroupAdded( null, field );
      }

      public void addFieldGroupField( Field field )
      {
         fieldGroupFieldAdded( null, field );
      }

      public Field getFieldGroupField()
      {
         return data.fieldGroupField().get();
      }

      public void fieldGroupAdded( @Optional DomainEvent event, Field field )
      {
         data.fieldGroup().set( field );

      }

      public void fieldGroupFieldAdded( @Optional DomainEvent event, Field field )
      {
         data.fieldGroupField().set( field );
      }
   }
}