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

package se.streamsource.streamflow.web.context.organizations.forms;

import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RequiresRoles;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.ListBoxFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.SelectionFieldValue;
import se.streamsource.streamflow.domain.form.TextAreaFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.resource.roles.BooleanDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.roles.NamedIndexDTO;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.NotableContext;
import se.streamsource.streamflow.web.domain.entity.form.FieldEntity;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.FieldId;
import se.streamsource.streamflow.web.domain.structure.form.FieldValueDefinition;
import se.streamsource.streamflow.web.domain.structure.form.Fields;
import se.streamsource.streamflow.web.domain.structure.form.Hint;
import se.streamsource.streamflow.web.domain.structure.form.Mandatory;

/**
 * JAVADOC
 */
@Mixins(FormFieldContext.Mixin.class)
public interface FormFieldContext
      extends DeleteContext, DescribableContext, NotableContext, Context
{
   public FieldDefinitionValue field();

   public void changemandatory( BooleanDTO mandatory );

   public void changefieldid( StringValue id );
   
   public void changehint( StringValue hint );

   @RequiresRoles(TextFieldValue.class)
   public void changewidth( IntegerDTO newWidth );

   @RequiresRoles(TextFieldValue.class)
   public void changeregularexpression( StringValue regularExpression );

   @RequiresRoles(TextAreaFieldValue.class)
   public void changerows( IntegerDTO newRows );

   @RequiresRoles(TextAreaFieldValue.class)
   public void changecols( IntegerDTO newRows );

   @RequiresRoles(NumberFieldValue.class)
   public void changeinteger( BooleanDTO integerDto );

   @RequiresRoles(SelectionFieldValue.class)
   public void addselectionelement( StringValue name );

   @RequiresRoles(SelectionFieldValue.class)
   public void removeselectionelement( IntegerDTO index );

   @RequiresRoles(SelectionFieldValue.class)
   public void moveselectionelement( NamedIndexDTO moveElement );

   @RequiresRoles(SelectionFieldValue.class)
   public void changeselectionelementname( NamedIndexDTO newNameDTO );

   public void move( StringValue direction );

   abstract class Mixin
         extends ContextMixin
         implements FormFieldContext
   {
      @Structure
      Module module;

      public FieldDefinitionValue field()
      {
         FieldEntity fieldEntity = roleMap.get( FieldEntity.class );

         ValueBuilder<FieldDefinitionValue> builder = module.valueBuilderFactory().newValueBuilder( FieldDefinitionValue.class );
         builder.prototype().field().set( EntityReference.getEntityReference( fieldEntity ) );
         builder.prototype().note().set( fieldEntity.note().get() );
         builder.prototype().description().set( fieldEntity.description().get() );
         builder.prototype().fieldId().set( fieldEntity.fieldId().get() );
         builder.prototype().fieldValue().set( fieldEntity.fieldValue().get() );
         builder.prototype().mandatory().set( fieldEntity.isMandatory() );
         builder.prototype().hint().set( fieldEntity.hint().get() );

         return builder.newInstance();
      }

      public void changemandatory( BooleanDTO mandatory )
      {
         Mandatory mandatoryField = roleMap.get( Mandatory.class );

         mandatoryField.changeMandatory( mandatory.bool().get() );
      }

      public void changefieldid( StringValue id )
      {
         FieldId fieldId = roleMap.get( FieldId.class );

         fieldId.changeFieldId( id.string().get() );
      }

      public void changehint( StringValue hint )
      {
         Hint hintField = roleMap.get( Hint.class );
         hintField.changeHint( hint.string().get() );
      }

      public void changewidth( IntegerDTO newWidth )
      {
         FieldValueDefinition fieldValueDefinition = roleMap.get( FieldValueDefinition.class );
         TextFieldValue value = roleMap.get( TextFieldValue.class );

         ValueBuilder<TextFieldValue> builder = value.buildWith();
         builder.prototype().width().set( newWidth.integer().get() );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void changeregularexpression( StringValue regularExpression )
      {
         FieldValueDefinition fieldValueDefinition = roleMap.get( FieldValueDefinition.class );
         TextFieldValue value = roleMap.get( TextFieldValue.class );

         ValueBuilder<TextFieldValue> builder = value.buildWith();
         builder.prototype().regularExpression().set( regularExpression.string().get() );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void changerows( IntegerDTO newRows )
      {
         FieldValueDefinition fieldValueDefinition = roleMap.get( FieldValueDefinition.class );
         TextAreaFieldValue value = roleMap.get( TextAreaFieldValue.class );

         ValueBuilder<TextAreaFieldValue> builder = value.buildWith();
         builder.prototype().rows().set( newRows.integer().get() );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void changecols( IntegerDTO newRows )
      {
         FieldValueDefinition fieldValueDefinition = roleMap.get( FieldValueDefinition.class );
         TextAreaFieldValue value = roleMap.get( TextAreaFieldValue.class );

         ValueBuilder<TextAreaFieldValue> builder = value.buildWith();
         builder.prototype().cols().set( newRows.integer().get() );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void changeinteger( BooleanDTO integerDto )
      {
         FieldValueDefinition definition = roleMap.get( FieldValueDefinition.class );
         NumberFieldValue value = roleMap.get( NumberFieldValue.class );

         ValueBuilder<NumberFieldValue> builder = value.buildWith();
         builder.prototype().integer().set( integerDto.bool().get() );

         definition.changeFieldValue( builder.newInstance() );
      }

      public void addselectionelement( StringValue name )
      {
         FieldValueDefinition fieldValueDefinition = roleMap.get( FieldValueDefinition.class );
         SelectionFieldValue value = roleMap.get( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         builder.prototype().values().get().add( name.string().get() );
         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void removeselectionelement( IntegerDTO index )
      {
         FieldValueDefinition fieldValueDefinition = roleMap.get( FieldValueDefinition.class );
         SelectionFieldValue value = roleMap.get( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         if (builder.prototype().values().get().size() > index.integer().get())
         {
            builder.prototype().values().get().remove( index.integer().get().intValue() );
            fieldValueDefinition.changeFieldValue( builder.newInstance() );
         }
      }

      public void moveselectionelement( NamedIndexDTO moveElement )
      {
         FieldValueDefinition fieldValueDefinition = roleMap.get( FieldValueDefinition.class );
         SelectionFieldValue value = roleMap.get( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         String element = builder.prototype().values().get().remove( moveElement.index().get().intValue() );
         if ("up".equals( moveElement.name().get() ))
         {
            builder.prototype().values().get().add( moveElement.index().get() - 1, element );
         } else
         {
            builder.prototype().values().get().add( moveElement.index().get() + 1, element );
         }
         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void changeselectionelementname( NamedIndexDTO newNameDTO )
      {
         FieldValueDefinition fieldValueDefinition = roleMap.get( FieldValueDefinition.class );
         SelectionFieldValue value = roleMap.get( SelectionFieldValue.class );

         ValueBuilder<SelectionFieldValue> builder = value.buildWith();
         builder.prototype().values().get().set( newNameDTO.index().get(), newNameDTO.name().get() );

         fieldValueDefinition.changeFieldValue( builder.newInstance() );
      }

      public void move( StringValue direction )
      {
         Field field = roleMap.get( Field.class );
         Fields fields = roleMap.get( Fields.class );
         Fields.Data fieldsData = roleMap.get( Fields.Data.class );

         int index = fieldsData.fields().toList().indexOf( field );
         if (direction.string().get().equalsIgnoreCase( "up" ))
         {
            try
            {
               fields.moveField( field, index - 1 );
            } catch (ConstraintViolationException e)
            {
            }
         } else
         {
            fields.moveField( field, index + 1 );
         }
      }

      public void delete()
      {
         Field field = roleMap.get( Field.class );
         Fields fields = roleMap.get( Fields.class );

         fields.removeField( field );
      }
   }
}
