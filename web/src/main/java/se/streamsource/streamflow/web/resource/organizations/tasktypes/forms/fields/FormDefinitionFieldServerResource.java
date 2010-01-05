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

package se.streamsource.streamflow.web.resource.organizations.tasktypes.forms.fields;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.domain.form.TextAreaFieldValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.roles.BooleanDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.form.FieldEntity;
import se.streamsource.streamflow.web.domain.form.FormEntity;
import se.streamsource.streamflow.web.domain.form.FormsQueries;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /organizations/{organization}/tasktypes/{forms}/forms/{form}/fields/{index}
 */
public class FormDefinitionFieldServerResource
      extends CommandQueryServerResource
{
   public FieldDefinitionValue field()
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      ValueBuilder<FieldDefinitionValue> builder = vbf.newValueBuilder( FieldDefinitionValue.class );
      builder.prototype().field().set( EntityReference.getEntityReference( field ) );
      builder.prototype().note().set( field.note().get() );
      builder.prototype().description().set( field.description().get() );
      builder.prototype().fieldValue().set( field.fieldValue().get() );

      return builder.newInstance();
   }

   public void updatemandatory( BooleanDTO mandatory )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      FieldValue fieldValue = vbf.newValueBuilder( FieldValue.class ).withPrototype( field.fieldValue().get() ).prototype();
      fieldValue.mandatory().set( mandatory.bool().get() );
      field.fieldValue().set( fieldValue );
   }

   public void changedescription( StringDTO newDescription )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      field.changeDescription( newDescription.string().get() );
   }

   public void changenote( StringDTO newNote )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      field.changeNote( newNote.string().get() );
   }

   public void changewidth( IntegerDTO newWidth )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      FieldValue value = field.fieldValue().get();
      if (value instanceof TextAreaFieldValue)
      {
         field.changeFieldValue( update( (TextAreaFieldValue) value, TextAreaFieldValue.class, newWidth) );
      }
      else if ( value instanceof TextFieldValue)
      {
         field.changeFieldValue( update( (TextFieldValue) value, TextFieldValue.class, newWidth) );
      }
   }

   private <T extends TextFieldValue> T update(T value, Class<T> valueType, IntegerDTO newWidth)
   {
      ValueBuilder<T> builder =
            vbf.newValueBuilder( valueType ).withPrototype( value );
      builder.prototype().width().set( newWidth.integer().get() );
      return builder.newInstance();
   }

   public void changerows( IntegerDTO newRows )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      FieldValue value = field.fieldValue().get();
      if ( value instanceof TextAreaFieldValue)
      {
         ValueBuilder<TextAreaFieldValue> builder =
               vbf.newValueBuilder( TextAreaFieldValue.class ).withPrototype( (TextAreaFieldValue) value );
         builder.prototype().rows().set( newRows.integer().get() );
         field.changeFieldValue( builder.newInstance() );
      }
   }

   public void deleteOperation()
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      form.removeField( field );
   }

   public void move( IntegerDTO newIndex )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      form.moveField( field, newIndex.integer().get() );
   }

}