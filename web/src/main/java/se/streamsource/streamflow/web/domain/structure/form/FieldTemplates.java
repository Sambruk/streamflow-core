/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(FieldTemplates.Mixin.class)
public interface FieldTemplates
{
   Field createFieldTemplate( String name, FieldValue valueDefinition );

   void removeFieldDefinition( Field field );

   interface Data
   {
      ManyAssociation<Field> fieldDefinitions();

      Field createdFieldDefinition( DomainEvent event, String id, FieldValue valueDefinition );

      void addedFieldDefinition( DomainEvent event, Field field );

      void removedFieldDefinition( DomainEvent event, Field field );

      Field getFieldDefinitionByName( String name );
   }

   abstract class Mixin
         implements FieldTemplates, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      public Field createFieldTemplate( String name, FieldValue valueDefinition )
      {
         String id = idGen.generate( Identity.class );

         Field field = createdFieldDefinition( DomainEvent.CREATE, id, valueDefinition );
         addedFieldDefinition( DomainEvent.CREATE, field );
         field.changeDescription( name );

         return field;
      }

      public Field createdFieldDefinition( DomainEvent event, String id, FieldValue valueDefinition )
      {
         EntityBuilder<Field> builder = uowf.currentUnitOfWork().newEntityBuilder( Field.class, id );

         builder.instanceFor(FieldValueDefinition.Data.class).fieldValue().set( valueDefinition );

         return builder.newInstance();
      }

      public Field getFieldDefinitionByName( String name )
      {
         return Describable.Mixin.getDescribable( fieldDefinitions(), name );
      }
   }
}