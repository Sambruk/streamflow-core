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

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.streamflow.api.administration.form.CheckboxesFieldValue;
import se.streamsource.streamflow.api.administration.form.ComboBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.CommentFieldValue;
import se.streamsource.streamflow.api.administration.form.DateFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.ListBoxFieldValue;
import se.streamsource.streamflow.api.administration.form.NumberFieldValue;
import se.streamsource.streamflow.api.administration.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.api.administration.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.api.administration.form.TextAreaFieldValue;
import se.streamsource.streamflow.api.administration.form.TextFieldValue;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldDTO;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.infrastructure.plugin.map.KartagoMapService;

/**
 * JAVADOC
 */
@Mixins(FormDraft.Mixin.class)
public interface FormDraft
{
   FormDraftDTO getFormDraftValue();

   void changeFormDraftValue( FormDraftDTO formDraftDTO);

   void changeFieldValue( EntityReference fieldId, String fieldValue );

   void addFormSignatureValue( FormSignatureDTO formSignatureDTO);

   void removeFormSignatures();

   void changeFieldAttachmentValue( AttachmentFieldDTO fieldAttachment );

   FieldSubmissionDTO getFieldValue( EntityReference fieldId );

   interface Data
   {
      Property<FormDraftDTO> formDraftValue();

      void changedFormDraft( @Optional DomainEvent event, FormDraftDTO formDraftDTO);

      void changedFieldValue( @Optional DomainEvent event, EntityReference fieldId, String fieldValue );

      void addedFormSignatureValue( @Optional DomainEvent event, FormSignatureDTO formSignatureDTO);

      void removedFormSignatures( @Optional DomainEvent event );

      void changedFieldAttachmentValue( @Optional DomainEvent event, AttachmentFieldDTO fieldAttachment );
   }

   abstract class Mixin
         implements FormDraft, Data
   {

      @Structure
      Module module;

      @Optional
      @Service
      KartagoMapService kartagoMapService;
      
      public FormDraftDTO getFormDraftValue()
      {
         return formDraftValue().get();
      }

      public void changeFormDraftValue( FormDraftDTO formDraftDTO)
      {
         changedFormDraft( null, formDraftDTO);
      }

      public void changeFieldValue( EntityReference fieldId, String newValue )
      {
         FormDraftDTO formDraft = formDraftValue().get();

         FieldSubmissionDTO field = findField( formDraft, fieldId );

         boolean update = false;
         if (field.value().get() != null && field.value().get().equals( newValue ))
         {
            return;
         } // Skip update - same value

         FieldValue value = field.field().get().fieldValue().get();
         if (value instanceof CheckboxesFieldValue)
         {
            update = validate( (CheckboxesFieldValue) value, newValue );
         } else if (value instanceof ComboBoxFieldValue)
         {
            update = validate( (ComboBoxFieldValue) value, newValue );
         } else if (value instanceof CommentFieldValue)
         {
            update = validate( (CommentFieldValue) value, newValue );
         } else if (value instanceof DateFieldValue)
         {
            update = validate( (DateFieldValue) value, newValue );
         } else if (value instanceof ListBoxFieldValue)
         {
            update = validate( (ListBoxFieldValue) value, newValue );
         } else if (value instanceof NumberFieldValue)
         {
            update = validate( (NumberFieldValue) value, newValue );
         } else if (value instanceof OptionButtonsFieldValue)
         {
            update = validate( (OptionButtonsFieldValue) value, newValue );
         } else if (value instanceof OpenSelectionFieldValue)
         {
            update = validate( (OpenSelectionFieldValue) value, newValue );
         } else if (value instanceof TextAreaFieldValue)
         {
            update = validate( (TextAreaFieldValue) value, newValue );
         } else if (value instanceof TextFieldValue)
         {
            update = validate( (TextFieldValue) value, newValue );
         }

         if (update)
         {
            changedFieldValue( null, fieldId, newValue );
         }
      }


      public void addFormSignatureValue( FormSignatureDTO formSignatureDTO)
      {
         //validate
         addedFormSignatureValue( null, formSignatureDTO);
      }

      public void removeFormSignatures()
      {
         removedFormSignatures( null );
      }

      private boolean validate( OpenSelectionFieldValue openSelectionFieldValue, String newValue )
      {
         return true;
      }

      private boolean validate( CheckboxesFieldValue definition, String value )
      {
         if ("".equals( value )) return true;
         return validateMultiple( definition.values().get(), value );
      }

      private boolean validateMultiple( List<String> values, String value )
      {
         String[] selections = value.split( ", " );
         for (String selection : selections)
         {
            if (!values.contains( selection ))
               return false;
         }
         return true;
      }

      private boolean validate( ComboBoxFieldValue definition, String value )
      {
         return true;
      }

      private boolean validate( CommentFieldValue definition, String value )
      {
         return false;
      }

      private boolean validate( DateFieldValue definition, String value )
      {
         try
         {
            DateFunctions.fromString( value );
            return true;
         } catch (IllegalStateException e)
         {
            return false;
         }
      }

      private boolean validate( ListBoxFieldValue definition, String value )
      {
         if ("".equals( value )) return true;
         return validateMultiple( definition.values().get(), value );
      }

      private boolean validate( NumberFieldValue definition, String value )
      {
         if ("".equals( value )) return true;
         try
         {
            // quick fix to make it accept ,
            value = value.replace( ',', '.' );
            Object o = (definition.integer().get() ? Integer.parseInt( value ) : Double.parseDouble( value ));
            return true;
         } catch (NumberFormatException e)
         {
            return false;
         }
      }

      private boolean validate( OptionButtonsFieldValue definition, String value )
      {
         return definition.values().get().contains( value );
      }

      private boolean validate( TextAreaFieldValue definition, String value )
      {
         return value != null;
      }

      private boolean validate( TextFieldValue definition, String value )
      {
         if (!Strings.empty( value ))
         {
            if (!Strings.empty( definition.regularExpression().get() ))
            {
               if (value != null)
               {
                  Pattern pattern = Pattern.compile( definition.regularExpression().get() );
                  Matcher matcher = pattern.matcher( value );

                  return matcher.matches();
               }
               return false;
            }
            return true;
         } else {
            return !definition.mandatory().get();
         }
      }

      private FieldSubmissionDTO findField( FormDraftDTO draft, EntityReference fieldRef )
      {
         for (PageSubmissionDTO pageSubmissionDTO : draft.pages().get())
         {
            for (FieldSubmissionDTO field : pageSubmissionDTO.fields().get())
            {
               if (field.field().get().field().get().equals( fieldRef ))
               {
                  return field;
               }
            }
         }
         return null;
      }

      public FieldSubmissionDTO getFieldValue( EntityReference fieldId )
      {
         return findField( formDraftValue().get(), fieldId );
      }

      public void changedFormDraft( DomainEvent event, FormDraftDTO formDraftDTO)
      {
         formDraftValue().set(formDraftDTO);
      }

      public void changedFieldValue( @Optional DomainEvent event, EntityReference fieldId, String fieldValue )
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();
         FieldSubmissionDTO field = findField( builder.prototype(), fieldId );
         field.value().set( fieldValue );

         formDraftValue().set( builder.newInstance() );
      }

      public void addedFormSignatureValue( @Optional DomainEvent event, FormSignatureDTO formSignatureDTO)
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();
         builder.prototype().signatures().get().add(formSignatureDTO);

         formDraftValue().set( builder.newInstance() );
      }

      public void removedFormSignatures( @Optional DomainEvent event )
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();
         builder.prototype().signatures().set( Collections.<FormSignatureDTO>emptyList() );

         formDraftValue().set( builder.newInstance() );
      }

      public void changeFieldAttachmentValue( AttachmentFieldDTO fieldAttachment )
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();

         FieldSubmissionDTO field = findField( builder.prototype(), fieldAttachment.field().get() );
         if (field != null)
         {
            changedFieldAttachmentValue( null, fieldAttachment );
         }
      }

      public void changedFieldAttachmentValue( DomainEvent event, AttachmentFieldDTO fieldAttachment )
      {
         ValueBuilder<FormDraftDTO> builder = formDraftValue().get().buildWith();
         FieldSubmissionDTO field = findField( builder.prototype(), fieldAttachment.field().get() );

         ValueBuilder<AttachmentFieldSubmission> valueBuilder = module.valueBuilderFactory().newValueBuilder(AttachmentFieldSubmission.class);
         valueBuilder.prototype().attachment().set( fieldAttachment.attachment().get() );
         valueBuilder.prototype().name().set( fieldAttachment.name().get() );

         field.value().set( valueBuilder.newInstance().toJSON() );

         formDraftValue().set( builder.newInstance() );
      }
   }

}