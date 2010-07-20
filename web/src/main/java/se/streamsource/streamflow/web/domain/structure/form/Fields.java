/**
 *
 * Copyright 2009-2010 Streamsource AB
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

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.constraints.annotation.GreaterThan;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

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

      Field createdField( DomainEvent event, String id, FieldValue value );

      void removedField( DomainEvent event, Field field );

      void movedField( DomainEvent event, Field field, int toIdx );
   }

   abstract class Mixin
         implements Fields, Data
   {
      @This
      Data data;

      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      public Field createField( String name, FieldValue fieldValue )
      {
         Field field = createdField( DomainEvent.CREATE, idGen.generate( Identity.class ), fieldValue );
         field.changeDescription( name );
         return field;
      }

      public void removeField( Field field )
      {
         if (!data.fields().contains( field ))
            return;

         removedField( DomainEvent.CREATE, field );
      }

      public void moveField( Field field, Integer toIdx )
      {
         if (!data.fields().contains( field ) || data.fields().count() <= toIdx)
            return;

         movedField( DomainEvent.CREATE, field, toIdx );
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

         EntityBuilder<Field> builder = uowf.currentUnitOfWork().newEntityBuilder( Field.class, id );
         builder.instanceFor(FieldValueDefinition.Data.class).fieldValue().set( fieldValue );

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
