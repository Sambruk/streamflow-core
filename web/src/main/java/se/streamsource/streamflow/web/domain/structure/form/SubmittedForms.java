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

import org.qi4j.api.common.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.property.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.value.*;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.web.domain.entity.attachment.*;
import se.streamsource.streamflow.web.domain.structure.attachment.*;

import java.util.*;

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

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Data state;

      @This
      FormDrafts submissions;

      @This
      FormAttachments formAttachments;

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
            ValueBuilder<SubmittedPageValue> pageBuilder = vbf.newValueBuilder(SubmittedPageValue.class);
            pageBuilder.prototype().page().set(pageValue.page().get());

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

                  // move current attachment from draft to case
                  if( field.field().get().fieldValue().get() instanceof AttachmentFieldValue )
                  {
                     try
                     { 
                        AttachmentFieldSubmission currentFormDraftAttachmentField = vbf.newValueFromJSON( AttachmentFieldSubmission.class, fieldBuilder.prototype().value().get() );
                        AttachmentEntity attachment = uowf.currentUnitOfWork().get( AttachmentEntity.class, currentFormDraftAttachmentField.attachment().get().identity() );
                        ((FormAttachments)formSubmission).moveAttachment( formAttachments, attachment );
                     } catch (ConstructionException e)
                     {
                        // ignore
                     }
                  }

                  // update effective field
                  EffectiveFieldValue effectiveFieldValue = effectiveValues.get( field.field().get().field().get() );
                  if (effectiveFieldValue == null || !effectiveFieldValue.value().get().equals( fieldBuilder.prototype().value().get() ))
                  {
                     eFieldBuilder.prototype().page().set( pageValue.page().get() );
                     eFieldBuilder.prototype().field().set( field.field().get().field().get() );
                     eFieldBuilder.prototype().value().set( fieldBuilder.prototype().value().get() );
                     effectiveValues.put( field.field().get().field().get(), eFieldBuilder.newInstance() );
                     effectiveFieldsChanged = true;
                  }

                  pageBuilder.prototype().fields().get().add( fieldBuilder.newInstance() );
               }
            }

            formBuilder.prototype().pages().get().add(pageBuilder.newInstance());
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
