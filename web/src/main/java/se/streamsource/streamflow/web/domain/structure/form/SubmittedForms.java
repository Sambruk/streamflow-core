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

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.EffectiveFieldValue;
import se.streamsource.streamflow.domain.form.EffectiveFormFieldsValue;
import se.streamsource.streamflow.domain.form.FieldSubmissionValue;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.SubmittedFieldValue;
import se.streamsource.streamflow.domain.form.SubmittedFormValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(SubmittedForms.Mixin.class)
public interface SubmittedForms
{
   void submitForm( FormSubmission formSubmission, Submitter submitter );

   interface Data
   {
      @UseDefaults
      Property<List<SubmittedFormValue>> submittedForms();

      @Optional
      Property<EffectiveFormFieldsValue> effectiveFieldValues();

      void submittedForm( DomainEvent event, FormSubmission formSubmission, Submitter submitter );

      String getEffectiveValue( Field field );
   }

   abstract class Mixin
         implements SubmittedForms, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      public void submitForm( FormSubmission formSubmission, Submitter submitter )
      {
         submittedForm( DomainEvent.CREATE, formSubmission, submitter );
      }

      public void submittedForm( DomainEvent event, FormSubmission formSubmission, Submitter submitter )
      {
         FormSubmissionValue value = formSubmission.getFormSubmission();
         ValueBuilder<SubmittedFormValue> formBuilder = vbf.newValueBuilder( SubmittedFormValue.class );

         formBuilder.prototype().submitter().set( EntityReference.getEntityReference( submitter ) );
         formBuilder.prototype().form().set( value.form().get() );
         formBuilder.prototype().submissionDate().set( new Date() );

         ValueBuilder<SubmittedFieldValue> fieldBuilder = vbf.newValueBuilder( SubmittedFieldValue.class );
         for (PageSubmissionValue pageValue : value.pages().get())
         {
            for (FieldSubmissionValue field : pageValue.fields().get())
            {
               fieldBuilder.prototype().field().set( field.field().get().field().get() );
               if ( field.value().get() == null )
               {
                  fieldBuilder.prototype().value().set( "" );
               } else

               fieldBuilder.prototype().value().set( field.value().get() );

               formBuilder.prototype().values().get().add( fieldBuilder.newInstance() );
            }
         }

         List<SubmittedFormValue> forms = submittedForms().get();
         forms.add( formBuilder.newInstance() );
         submittedForms().set( forms );

         //Recalculate effective values
         ValueBuilder<EffectiveFieldValue> eFieldBuilder = vbf.newValueBuilder( EffectiveFieldValue.class );

         LinkedHashMap<EntityReference, EffectiveFieldValue> effectiveValues = new LinkedHashMap<EntityReference, EffectiveFieldValue>();
         for (SubmittedFormValue submittedFormValue : forms)
         {
            eFieldBuilder.prototype().submissionDate().set( submittedFormValue.submissionDate().get() );
            eFieldBuilder.prototype().submitter().set( submittedFormValue.submitter().get() );

            for (SubmittedFieldValue fieldValue : submittedFormValue.values().get())
            {
               EffectiveFieldValue effectiveFieldValue = effectiveValues.get( fieldValue.field().get() );
               if ( effectiveFieldValue != null )
               {
                  if ( !effectiveFieldValue.value().get().equals( fieldValue.value().get() ))
                  {
                     eFieldBuilder.prototype().field().set( fieldValue.field().get() );
                     eFieldBuilder.prototype().value().set( fieldValue.value().get() );
                     effectiveValues.put( fieldValue.field().get(), eFieldBuilder.newInstance() );
                  }

               } else
               {
                  eFieldBuilder.prototype().field().set( fieldValue.field().get() );
                  eFieldBuilder.prototype().value().set( fieldValue.value().get() );
                  effectiveValues.put( fieldValue.field().get(), eFieldBuilder.newInstance() );
               }
            }
         }

         ValueBuilder<EffectiveFormFieldsValue> fieldsBuilder = vbf.newValueBuilder( EffectiveFormFieldsValue.class );
         List<EffectiveFieldValue> effectiveFieldValues = fieldsBuilder.prototype().fields().get();
         effectiveFieldValues.addAll( effectiveValues.values() );

         EffectiveFormFieldsValue effectiveFormFieldsValue = fieldsBuilder.newInstance();

         effectiveFieldValues().set( effectiveFormFieldsValue );
      }

      public String getEffectiveValue( Field field )
      {
         EffectiveFormFieldsValue effectiveFormFieldsValue = effectiveFieldValues().get();
         if (effectiveFormFieldsValue == null)
         {
            return null;
         }

         // Find value among effective fields collection
         EntityReference fieldRef = EntityReference.getEntityReference( field );
         for (EffectiveFieldValue effectiveFieldValue : effectiveFieldValues().get().fields().get())
         {
            if (effectiveFieldValue.field().get().equals( fieldRef ))
               return effectiveFieldValue.value().get();
         }

         // No such field has been submitted
         return null;
      }
   }
}
