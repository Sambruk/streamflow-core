/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;

import se.streamsource.streamflow.api.administration.form.FieldGroupFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Notable;


/**
 * These are the actual instances of the fields
 * of a field group to be used on the form draft.
 * This "instantiation" is necessary if several field groups
 * of the same type is added to a form
 */
@Mixins(FieldGroupFieldsInstance.Mixin.class)
public interface FieldGroupFieldsInstance
{
   void addFieldGroupFields( Field field );

   void removeFieldGroupFields( Field field );

   List<Field> listFieldGroupFields( Field field );

   interface Data
   {
      @Aggregated
      ManyAssociation<FieldGroupFieldInstance> groupFields();

      Field fieldGroupFieldsAdded( @Optional DomainEvent event, Field fieldGroup, String id, Field fieldGroupField);
      
      FieldGroupFieldInstance fieldGroupFieldInstanceAdded(@Optional DomainEvent event, Field fieldGroup, String id, Field fieldGroupField);
      
      void fieldGroupFieldsRemoved( @Optional DomainEvent event, FieldGroupFieldInstance instance );
   }

   abstract class Mixin
         implements FieldGroupFieldsInstance, Data
   {
      @This
      Data data;

      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;


      public void addFieldGroupFields( Field field )
      {
         FieldGroupFieldValue value = (FieldGroupFieldValue) ((FieldValueDefinition.Data) field).fieldValue().get();
         // find field group definition, and recurse for each field
         Fields.Data fields = module.unitOfWorkFactory().currentUnitOfWork().get( Fields.Data.class, value.fieldGroup().get().identity() );
         for ( Field groupField : fields.fields() )
         {
            Field createdField = fieldGroupFieldsAdded( null, field, idGen.generate( Identity.class ), groupField );
            createdField.changeDescription( groupField.getDescription() );
            fieldGroupFieldInstanceAdded(null, field, idGen.generate( Identity.class ), createdField );
         }
      }

      public void removeFieldGroupFields( Field field )
      {
         List<FieldGroupFieldInstance> toBeRemoved = new ArrayList<FieldGroupFieldInstance>(  );
         for (FieldGroupFieldInstance instance : data.groupFields())
         {
            Field theField = ((FieldGroupValue.Data) instance).fieldGroup().get();
            if ( EntityReference.getEntityReference( theField ).identity().equals(
                 EntityReference.getEntityReference( field ).identity() ))
            {
               toBeRemoved.add( instance );
            }
         }
         for (FieldGroupFieldInstance instance : toBeRemoved)
         {
            fieldGroupFieldsRemoved( null, instance );
         }
      }

      public Field fieldGroupFieldsAdded( @Optional DomainEvent event, Field fieldGroup, String id, Field fieldGroupField )
      {

         FieldValueDefinition.Data definition = (FieldValueDefinition.Data) fieldGroupField;
         
         EntityBuilder<Field> fieldBuilder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( Field.class, id );
         fieldBuilder.instanceFor(Mandatory.Data.class).mandatory().set( fieldGroupField.isMandatory() );
         fieldBuilder.instanceFor(Notable.Data.class).note().set( fieldGroupField.getNote() );
         fieldBuilder.instanceFor(Describable.Data.class).description().set( fieldGroupField.getDescription() );
         fieldBuilder.instanceFor(Datatype.Data.class).datatype().set( ((Datatype.Data)fieldGroupField).datatype().get() );
         fieldBuilder.instanceFor(Statistical.Data.class).statistical().set( ((Statistical.Data)fieldGroupField).statistical().get() );
         
         FieldValue fieldValue = definition.fieldValue().get();
         fieldBuilder.instanceFor(FieldValueDefinition.Data.class).fieldValue().set( fieldValue );
         
         
         String fieldId = Classes.interfacesOf( fieldValue.getClass()).iterator().next().getSimpleName();
         fieldId = fieldGroup.getDescription() + "_" + fieldId.substring( 0, fieldId.length()-"FieldValue".length() );
         fieldId += data.groupFields().count()+1;
         fieldBuilder.instanceFor(FieldId.Data.class).fieldId().set( fieldId );

         Field createdFieldGroupField = fieldBuilder.newInstance();

         return createdFieldGroupField;
      }

      public FieldGroupFieldInstance fieldGroupFieldInstanceAdded(@Optional DomainEvent event, Field fieldGroup, String id, Field fieldGroupField){
         EntityBuilder<FieldGroupFieldInstance> builder = module.unitOfWorkFactory().currentUnitOfWork().newEntityBuilder( FieldGroupFieldInstance.class, id );
         builder.instance().addFieldGroup( fieldGroup );
         builder.instance().addFieldGroupField( fieldGroupField );
         FieldGroupFieldInstance field = builder.newInstance();

         data.groupFields().add( field );
         return field;
      }
      
      public void fieldGroupFieldsRemoved( @Optional DomainEvent event, FieldGroupFieldInstance instance )
      {
         data.groupFields().remove( instance );
      }

      public List<Field> listFieldGroupFields( Field field )
      {
         List<Field> groupFields = new ArrayList<Field>( );
         for (FieldGroupFieldInstance instance : data.groupFields())
         {
            Field theField = ((FieldGroupValue.Data) instance).fieldGroup().get();
            if ( EntityReference.getEntityReference( theField ).identity().equals(
                  EntityReference.getEntityReference( field ).identity() ))
            {
               groupFields.add( instance.getFieldGroupField() );
            }
         }
         return groupFields;
      }
   }
}
