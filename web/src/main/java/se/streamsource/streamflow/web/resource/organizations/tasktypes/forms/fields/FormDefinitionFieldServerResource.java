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
import se.streamsource.streamflow.domain.form.CommentFieldValue;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.SelectionFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.PageBreakFieldValue;
import se.streamsource.streamflow.resource.roles.BooleanDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.roles.NamedIndexDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

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

   public void updatemandatory( BooleanDTO mandatory )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      ValueBuilder<? extends FieldValue> builder = getBuilderOfType( field.fieldValue().get() );
      builder.prototype().mandatory().set( mandatory.bool().get() );

      field.changeFieldValue( builder.newInstance() );
   }

   private ValueBuilder<? extends FieldValue> getBuilderOfType(FieldValue field)
   {
      if ( field instanceof TextFieldValue)
      {
         return vbf.newValueBuilder( TextFieldValue.class ).withPrototype( (TextFieldValue) field );
      } else if (field instanceof DateFieldValue)
      {
         return vbf.newValueBuilder( DateFieldValue.class ).withPrototype( (DateFieldValue) field );
      } else if ( field instanceof NumberFieldValue)
      {
         return vbf.newValueBuilder( NumberFieldValue.class ).withPrototype( (NumberFieldValue) field );
      } else if ( field instanceof SelectionFieldValue)
      {
         return vbf.newValueBuilder( SelectionFieldValue.class ).withPrototype( (SelectionFieldValue) field );
      } else if ( field instanceof PageBreakFieldValue)
      {
         return vbf.newValueBuilder( PageBreakFieldValue.class ).withPrototype( (PageBreakFieldValue) field );
      } else if ( field instanceof CommentFieldValue)
      {
         return vbf.newValueBuilder( CommentFieldValue.class ).withPrototype( (CommentFieldValue) field );
      }
      return null;
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
      if ( value instanceof TextFieldValue)
      {
         ValueBuilder<TextFieldValue> builder =
               vbf.newValueBuilder( TextFieldValue.class ).withPrototype( (TextFieldValue) value );
         builder.prototype().width().set( newWidth.integer().get() );
         field.changeFieldValue( builder.newInstance() );
      }
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
      if ( value instanceof TextFieldValue)
      {
         ValueBuilder<TextFieldValue> builder =
               vbf.newValueBuilder( TextFieldValue.class ).withPrototype( (TextFieldValue) value );
         builder.prototype().rows().set( newRows.integer().get() );
         field.changeFieldValue( builder.newInstance() );
      }
   }

   public void changemultiple( BooleanDTO multiple)
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      FieldValue value = field.fieldValue().get();
      if ( value instanceof SelectionFieldValue)
      {
         ValueBuilder<SelectionFieldValue> builder =
               vbf.newValueBuilder( SelectionFieldValue.class ).withPrototype( (SelectionFieldValue) value );
         builder.prototype().multiple().set( multiple.bool().get() );
         field.changeFieldValue( builder.newInstance() );
      }
   }

   public void addselectionelement( StringDTO name )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      FieldValue value = field.fieldValue().get();
      if ( value instanceof SelectionFieldValue)
      {
         ValueBuilder<SelectionFieldValue> builder =
               vbf.newValueBuilder( SelectionFieldValue.class ).withPrototype( (SelectionFieldValue) value );
         builder.prototype().values().get().add( name.string().get() );
         field.changeFieldValue( builder.newInstance() );
      }
   }

   public void removeselectionelement( IntegerDTO index )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      FieldValue value = field.fieldValue().get();
      if ( value instanceof SelectionFieldValue)
      {
         ValueBuilder<SelectionFieldValue> builder =
               vbf.newValueBuilder( SelectionFieldValue.class ).withPrototype( (SelectionFieldValue) value );
         if (builder.prototype().values().get().size() > index.integer().get() )
         {
            builder.prototype().values().get().remove( index.integer().get().intValue() );
            field.changeFieldValue( builder.newInstance() );
         }
      }
   }

   public void moveselectionelement( NamedIndexDTO moveElement )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      FieldValue value = field.fieldValue().get();
      if ( value instanceof SelectionFieldValue)
      {
         ValueBuilder<SelectionFieldValue> builder =
               vbf.newValueBuilder( SelectionFieldValue.class ).withPrototype( (SelectionFieldValue) value );
         String element = builder.prototype().values().get().remove( moveElement.index().get().intValue() );
         if ( "up".equals( moveElement.name().get()) )
         {
            builder.prototype().values().get().add( moveElement.index().get()-1, element );
         } else
         {
            builder.prototype().values().get().add( moveElement.index().get()+1, element );
         }
         field.changeFieldValue( builder.newInstance() );
      }
   }

   public void changeselectionelementname( NamedIndexDTO newNameDTO )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      FieldValue value = field.fieldValue().get();
      if ( value instanceof SelectionFieldValue)
      {
         ValueBuilder<SelectionFieldValue> builder =
               vbf.newValueBuilder( SelectionFieldValue.class ).withPrototype( (SelectionFieldValue) value );
         builder.prototype().values().get().set( newNameDTO.index().get(), newNameDTO.name().get() );
         field.changeFieldValue( builder.newInstance() );
      }
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

   public void changecomment( StringDTO newComment )
   {
      String identity = getRequest().getAttributes().get( "form" ).toString();
      String fieldIndex = getRequest().getAttributes().get( "index" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      FormEntity form = uow.get( FormEntity.class, identity );
      checkPermission( form );
      FieldEntity field = (FieldEntity) form.fields().get( Integer.parseInt( fieldIndex ) );

      FieldValue value = field.fieldValue().get();
      if ( value instanceof CommentFieldValue )
      {
         ValueBuilder<CommentFieldValue> builder =
               vbf.newValueBuilder( CommentFieldValue.class ).withPrototype( (CommentFieldValue) value );
         builder.prototype().comment().set( newComment.string().get() );
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

}