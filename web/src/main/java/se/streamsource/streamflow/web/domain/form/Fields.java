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

package se.streamsource.streamflow.web.domain.form;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.library.constraints.annotation.GreaterThan;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Fields.Mixin.class)
public interface Fields
{
   FieldEntity createField( String name, FieldValue fieldValue );

   void removeField( Field field );

   void moveField( Field field, @GreaterThan(-1) Integer toIdx );

   interface Data
   {
      ManyAssociation<Field> fields();

      FieldEntity createdField( DomainEvent event, String id, FieldValue value );

      void removedField( DomainEvent event, Field field );

      void movedField( DomainEvent event, Field field, int toIdx );

      FieldEntity getFieldByName( String name );
   }

   abstract class Mixin
         implements Fields, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      public FieldEntity createField( String name, FieldValue fieldValue )
      {
         FieldEntity field = createdField( DomainEvent.CREATE, idGen.generate( FieldEntity.class ), fieldValue );
         field.changeDescription( name );
         return field;
      }

      public void removeField( Field field )
      {
         if (!fields().contains( field ))
            return;

         removedField( DomainEvent.CREATE, field );
      }

      public void moveField( Field field, Integer toIdx )
      {
         if (!fields().contains( field ) || fields().count() < toIdx)
            return;

         movedField( DomainEvent.CREATE, field, toIdx );
      }

      public FieldEntity getFieldByName( String name )
      {
         for (Field field : fields())
         {
            if (((Describable.Data) field).description().get().equals( name ))
               return (FieldEntity) field;
         }
         return null;
      }

      public FieldEntity createdField( DomainEvent event, String id, FieldValue fieldValue )
      {

         EntityBuilder<FieldEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( FieldEntity.class, id );
         builder.instance().fieldValue().set( fieldValue );

         FieldEntity field = builder.newInstance();

         fields().add( field );

         return field;
      }

      public void movedField( DomainEvent event, Field field, int toIdx )
      {
         fields().remove( field );

         fields().add( toIdx, field );
      }

      public void removedField( DomainEvent event, Field field )
      {
         fields().remove( field );
      }
   }
}
