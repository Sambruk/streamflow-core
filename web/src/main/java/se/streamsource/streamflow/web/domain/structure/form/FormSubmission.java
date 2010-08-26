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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.CheckboxesFieldValue;
import se.streamsource.streamflow.domain.form.ComboBoxFieldValue;
import se.streamsource.streamflow.domain.form.CommentFieldValue;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.ListBoxFieldValue;
import se.streamsource.streamflow.domain.form.NumberFieldValue;
import se.streamsource.streamflow.domain.form.OptionButtonsFieldValue;
import se.streamsource.streamflow.domain.form.TextAreaFieldValue;
import se.streamsource.streamflow.domain.form.TextFieldValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.util.Strings;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(FormSubmission.Mixin.class)
public interface FormSubmission
{
   FormSubmissionValue getFormSubmission();

   void changeFormSubmission( FormSubmissionValue formSubmission );

   void changeFieldValue( EntityReference fieldId, String newValue);

   interface Data
   {
      Property<FormSubmissionValue> formSubmissionValue();

      void changedFormSubmission( DomainEvent event, FormSubmissionValue formSubmission);
   }

   abstract class Mixin
         implements FormSubmission, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      public FormSubmissionValue getFormSubmission()
      {
         return formSubmissionValue().get();
      }

      public void changeFormSubmission( FormSubmissionValue formSubmission )
      {
         changedFormSubmission( DomainEvent.CREATE, formSubmission );
      }

      public void changeFieldValue( EntityReference fieldId, String newValue )
      {
         ValueBuilder<FormSubmissionValue> builder = formSubmissionValue().get().buildWith();

         int currentPage = builder.prototype().currentPage().get();

         List<FieldSubmissionValue> fields = builder.prototype().pages().get().get( currentPage ).fields().get();
         for (FieldSubmissionValue field : fields)
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
               } else if ( value instanceof TextAreaFieldValue)
               {
                  update = validate( (TextAreaFieldValue) value, newValue );
               } else if ( value instanceof TextFieldValue)
               {
                  update = validate( (TextFieldValue) value, newValue );
               }

               if ( update )
               {
                  field.value().set( newValue );
                  changedFormSubmission( DomainEvent.CREATE, builder.newInstance() );
                  return;
               }
            }
         }
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
         if ( definition.multiple().get() )
         {
            return validateMultiple( definition.values().get(), value );
         } else
         {
            return definition.values().get().contains( value );
         }
      }

      private boolean validate( NumberFieldValue definition, String value )
      {
         if ( "".equals( value )) return true;
         try
         {
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
         return value != null && value.length() <= definition.width().get();

      }


      public void changedFormSubmission( DomainEvent event, FormSubmissionValue formSubmission )
      {
         formSubmissionValue().set( formSubmission );
      }
   }

}