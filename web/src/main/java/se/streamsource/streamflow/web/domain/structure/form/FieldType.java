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

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.library.constraints.annotation.NotEmpty;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.entity.form.FieldTypeDefinitionEntity;

/**
 * Technical type for a field. May not be empty
 */
@Mixins(FieldType.Mixin.class)
public interface FieldType
{
   void changeFieldType( Association<FieldTypeDefinition> newType);

   interface Data
   {
      @Optional
      Association<FieldTypeDefinition> fieldType();

      void changedFieldType( @Optional DomainEvent event, Association<FieldTypeDefinition> newType );
   }

   class Mixin
      implements FieldType
   {
      @This
      Data data;

      public void changeFieldType( Association<FieldTypeDefinition> newType)
      {
         data.changedFieldType( null, newType );
      }
   }

}