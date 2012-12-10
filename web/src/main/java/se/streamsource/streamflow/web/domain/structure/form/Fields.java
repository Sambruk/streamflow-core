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
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;
import org.qi4j.library.constraints.annotation.GreaterThan;

import se.streamsource.streamflow.api.administration.form.FieldGroupFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.Describable;

/**
 * JAVADOC
 */
@Mixins(Fields.Mixin.class)
public interface Fields
{
   Field createField( String name, FieldValue fieldValue );

   void removeField( Field field );

   void moveField( Field field, @GreaterThan(-1) Integer toIdx );

   Field getFieldByName( String name );

   interface Data
   {
      @Aggregated
      ManyAssociation<Field> fields();

      Field createdField( @Optional DomainEvent event, String id, FieldValue value );

      void removedField( @Optional DomainEvent event, Field field );

      void movedField( @Optional DomainEvent event, Field field, int toIdx );
   }

   abstract class Mixin
         implements Fields, Data
   {
      @This
      Data data;

      @This
      FieldGroupFieldsInstance fieldGroupFields;

      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

      public Field createField( String name, FieldValue fieldValue )
      {
         Field field = createdField( null, idGen.generate( Identity.class ), fieldValue );
         if ( fieldValue instanceof FieldGroupFieldValue )
         {
            fieldGroupFields.addFieldGroupFields( field );
         }
         field.changeDescription( name );
         return field;
      }

      public void removeField( Field field )
      {
         if (!data.fields().contains( field ))
            return;

         if ( ((FieldValueDefinition.Data) field).fieldValue().get() instanceof FieldGroupFieldValue )
         {
            fieldGroupFields.removeFieldGroupFields( field );
         }
         removedField( null, field );
      }

      public void moveField( Field field, Integer toIdx )
      {
         if (!data.fields().contains( field ) || data.fields().count() <= toIdx)
            return;

         movedField( null, field, toIdx );
      }

      public Field getFieldByName( String name )
      {
         for (Field field : data.fields())
         {
            if (((Describable.Data) field).description().get().equals( name ))
               return field;
         }
         return null;
      }

      public Field createdField( DomainEvent event, String id, FieldValue fieldValue )
      {

         EntityBuilder<Field> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( Field.class, id );
         builder.instanceFor(FieldValueDefinition.Data.class).fieldValue().set( fieldValue );
         String fieldId = Classes.interfacesOf( fieldValue.getClass()).iterator().next().getSimpleName();
         fieldId = fieldId.substring( 0, fieldId.length()-"FieldValue".length() );
         fieldId += data.fields().count()+1;
         builder.instanceFor(FieldId.Data.class).fieldId().set( fieldId );

         Field field = builder.newInstance();

         data.fields().add( field );

         return field;
      }

      public void movedField( DomainEvent event, Field field, int toIdx )
      {
         data.fields().remove( field );

         data.fields().add( toIdx, field );
      }

      public void removedField( DomainEvent event, Field field )
      {
         data.fields().remove( field );
      }
   }
}
