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
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(SubmittedForms.Mixin.class)
public interface SubmittedForms
{
   void submitForm( FormDraft formSubmission, Submitter submitter );

   boolean hasSubmittedForms();

   interface Data
   {
      @UseDefaults
      @Queryable(false)
      Property<List<SubmittedFormValue>> submittedForms();

      @Optional
      @Queryable(false)
      Property<EffectiveFormFieldsValue> effectiveFieldValues();

      void submittedForm( @Optional DomainEvent event, EffectiveFormFieldsValue effectiveFieldsValue, SubmittedFormValue form );
   }

   abstract class Mixin
         implements SubmittedForms, Data
   {
      @Structure
      ValueBuilderFactory vbf;

      @This
      Data state;

      @This
      FormDrafts submissions;

      public void submitForm( FormDraft formSubmission, Submitter submitter )
      {
         boolean effectiveFieldsChanged = false;
         ValueBuilder<EffectiveFieldValue> eFieldBuilder = vbf.newValueBuilder( EffectiveFieldValue.class );

         LinkedHashMap<EntityReference, EffectiveFieldValue> effectiveValues = new LinkedHashMap<EntityReference, EffectiveFieldValue>();
         if ( effectiveFieldValues().get() != null)
         {
            for (EffectiveFieldValue fieldValue : effectiveFieldValues().get().fields().get())
            {
               effectiveValues.put( fieldValue.field().get(), fieldValue );
            }
         }

         FormDraftValue value = formSubmission.getFormDraftValue();
         ValueBuilder<SubmittedFormValue> formBuilder = vbf.newValueBuilder( SubmittedFormValue.class );

         formBuilder.prototype().submitter().set( EntityReference.getEntityReference( submitter ) );
         formBuilder.prototype().form().set( value.form().get() );
         formBuilder.prototype().submissionDate().set( new Date() );

         eFieldBuilder.prototype().form().set( formSubmission.getFormDraftValue().form().get() );
         eFieldBuilder.prototype().submissionDate().set( formBuilder.prototype().submissionDate().get() );
         eFieldBuilder.prototype().submitter().set( EntityReference.getEntityReference( submitter ) );

         ValueBuilder<SubmittedFieldValue> fieldBuilder = vbf.newValueBuilder( SubmittedFieldValue.class );
         for (PageSubmissionValue pageValue : value.pages().get())
         {
            for (FieldSubmissionValue field : pageValue.fields().get())
            {
               // ignore comment fields when submitting
               if ( !(field.field().get().fieldValue().get() instanceof CommentFieldValue) )
               {
                  fieldBuilder.prototype().field().set( field.field().get().field().get() );
                  if ( field.value().get() == null )
                  {
                     fieldBuilder.prototype().value().set( "" );
                  } else
                  {
                     fieldBuilder.prototype().value().set( field.value().get() );
                  }

                  // update effective field
                  EffectiveFieldValue effectiveFieldValue = effectiveValues.get( field.field().get().field().get() );
                  if (effectiveFieldValue == null || !effectiveFieldValue.value().get().equals( fieldBuilder.prototype().value().get() ))
                  {
                     eFieldBuilder.prototype().field().set( field.field().get().field().get() );
                     eFieldBuilder.prototype().value().set( fieldBuilder.prototype().value().get() );
                     effectiveValues.put( field.field().get().field().get(), eFieldBuilder.newInstance() );
                     effectiveFieldsChanged = true;
                  }

                  formBuilder.prototype().values().get().add( fieldBuilder.newInstance() );
               }
            }
         }

         // update the effective fields and submitted forms
         // only do this if effective fields has changed
         if ( effectiveFieldsChanged )
         {
            ValueBuilder<EffectiveFormFieldsValue> fieldsBuilder = vbf.newValueBuilder( EffectiveFormFieldsValue.class );
            List<EffectiveFieldValue> effectiveFieldValues = fieldsBuilder.prototype().fields().get();
            effectiveFieldValues.addAll( effectiveValues.values() );

            EffectiveFormFieldsValue effectiveFormFieldsValue = fieldsBuilder.newInstance();

            submittedForm( null, effectiveFormFieldsValue, formBuilder.newInstance() );
         }

         // Now discard it
         submissions.discardFormDraft( formSubmission );
      }


      public void submittedForm( @Optional DomainEvent event, EffectiveFormFieldsValue effectiveFieldsValue, SubmittedFormValue form )
      {
         effectiveFieldValues().set( effectiveFieldsValue );
         List<SubmittedFormValue> forms = submittedForms().get();
         forms.add( form );
         submittedForms().set( forms );
      }

      public boolean hasSubmittedForms()
      {
         return !state.submittedForms().get().isEmpty();
      }
   }
}
