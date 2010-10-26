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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.form.CheckboxesFieldValue;
import se.streamsource.streamflow.domain.form.ComboBoxFieldValue;
import se.streamsource.streamflow.domain.form.CommentFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.FormDraftValue;
import se.streamsource.streamflow.domain.form.FormSignatureValue;
import se.streamsource.streamflow.domain.form.ListBoxFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.OpenSelectionFieldValue;
import se.streamsource.streamflow.domain.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.domain.form.TextAreaFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.util.Strings;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JAVADOC
 */
@Mixins(FormDraft.Mixin.class)
public interface FormDraft
{
   FormDraftValue getFormDraftValue();

   void changeFormDraftValue( FormDraftValue formDraftValue );

   void changeFieldValue( EntityReference fieldId, String fieldValue );

   void addFormSignatureValue( FormSignatureValue formSignatureValue );

   void removeFormSignatures( );

   interface Data
   {
      Property<FormDraftValue> formDraftValue();

      void changedFormDraft( DomainEvent event, FormDraftValue formDraftValue);

      void changedFieldValue( DomainEvent event, EntityReference fieldId, String fieldValue );

      void addedFormSignatureValue( DomainEvent event, FormSignatureValue formSignatureValue);

      void removedFormSignatures( DomainEvent event );
   }

   abstract class Mixin
         implements FormDraft, Data
   {
      public FormDraftValue getFormDraftValue()
      {
         return formDraftValue().get();
      }

      public void changeFormDraftValue( FormDraftValue formDraftValue )
      {
         changedFormDraft( DomainEvent.CREATE, formDraftValue );
      }

      public void changeFieldValue( EntityReference fieldId, String newValue )
      {
         FormDraftValue formDraft = formDraftValue().get();

         for (PageSubmissionValue pageSubmissionValue : formDraft.pages().get())
         {
            for (FieldSubmissionValue field : pageSubmissionValue.fields().get())
            {
               if (field.field().get().field().get().equals(fieldId))
               {
                  boolean update = false;
                  if (field.value().get() != null && field.value().get().equals( newValue ))
                  {
                     return;
                  } // Skip update - same value

                  FieldValue value = field.field().get().fieldValue().get();
                  if ( value instanceof CheckboxesFieldValue )
                  {
                     update = validate( (CheckboxesFieldValue) value, newValue );
                  } else if ( value instanceof ComboBoxFieldValue)
                  {
                     update = validate( (ComboBoxFieldValue) value, newValue );
                  } else if ( value instanceof CommentFieldValue)
                  {
                     update = validate( (CommentFieldValue) value, newValue );
                  } else if ( value instanceof DateFieldValue)
                  {
                     update = validate( (DateFieldValue) value, newValue );
                  } else if ( value instanceof ListBoxFieldValue)
                  {
                     update = validate( (ListBoxFieldValue) value, newValue );
                  } else if ( value instanceof NumberFieldValue)
                  {
                     update = validate( (NumberFieldValue) value, newValue );
                  } else if ( value instanceof OptionButtonsFieldValue)
                  {
                     update = validate( (OptionButtonsFieldValue) value, newValue );
                  } else if ( value instanceof OpenSelectionFieldValue)
                  {
                     update = validate( (OpenSelectionFieldValue) value, newValue );
                  } else if ( value instanceof TextAreaFieldValue)
                  {
                     update = validate( (TextAreaFieldValue) value, newValue );
                  } else if ( value instanceof TextFieldValue)
                  {
                     update = validate( (TextFieldValue) value, newValue );
                  }

                  if ( update )
                  {
                     changedFieldValue( DomainEvent.CREATE, fieldId, newValue );
                  }
                  return;
               }
            }
         }
      }


      public void addFormSignatureValue( FormSignatureValue formSignatureValue )
      {
         //validate
         addedFormSignatureValue( DomainEvent.CREATE, formSignatureValue );
      }

      public void removeFormSignatures()
      {
         removedFormSignatures( DomainEvent.CREATE );
      }

      private boolean validate( OpenSelectionFieldValue openSelectionFieldValue, String newValue )
      {
         return true;
      }

      private boolean validate( CheckboxesFieldValue definition, String value )
      {
         if ( "".equals( value )) return true;
         return validateMultiple( definition.values().get(), value );
      }

      private boolean validateMultiple( List<String> values, String value )
      {
         String[] selections = value.split( ", " );
         for (String selection : selections )
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
         if ( "".equals( value )) return true;
         return validateMultiple( definition.values().get(), value );
      }

      private boolean validate( NumberFieldValue definition, String value )
      {
         if ( "".equals( value )) return true;
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
         if (Strings.notEmpty( definition.regularExpression().get() ))
         {
            if ( value != null )
            {
               Pattern pattern = Pattern.compile( definition.regularExpression().get() );
               Matcher matcher = pattern.matcher( value );

               return matcher.matches();
            }
            return false;
         }

         return value != null;
      }


      public void changedFormDraft( DomainEvent event, FormDraftValue formDraftValue )
      {
         formDraftValue().set( formDraftValue );
      }

      public void changedFieldValue( DomainEvent event, EntityReference fieldId, String fieldValue )
      {
         ValueBuilder<FormDraftValue> builder = formDraftValue().get().buildWith();
         for (PageSubmissionValue page : builder.prototype().pages().get())
         {
            for (FieldSubmissionValue field : page.fields().get())
            {
               if (field.field().get().field().get().equals(fieldId))
               {
                  field.value().set( fieldValue );
               }
            }
         }

         formDraftValue().set( builder.newInstance() );
      }

      public void addedFormSignatureValue( DomainEvent event, FormSignatureValue formSignatureValue )
      {
         formDraftValue().get().signatures().get().add( formSignatureValue );
      }

      public void removedFormSignatures( DomainEvent event )
      {
         formDraftValue().get().signatures().set( Collections.<FormSignatureValue>emptyList() );
      }
   }

}