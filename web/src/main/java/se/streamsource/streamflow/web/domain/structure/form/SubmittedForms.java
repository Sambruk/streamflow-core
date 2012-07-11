/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.CommentFieldValue;
import se.streamsource.streamflow.api.administration.form.FieldGroupFieldValue;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.attachment.AttachmentEntity;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.attachment.FormAttachments;

import java.util.Date;
import java.util.List;

/**
 * Maintains list of submitted forms on a case
 */
@Mixins(SubmittedForms.Mixin.class)
public interface SubmittedForms
{
   void submitForm( FormDraft formSubmission, Submitter submitter );

   /**
    * Check if there are any submitted forms at all
    *
    * @return
    */
   boolean hasSubmittedForms();

   interface Data
   {
      @UseDefaults
      @Queryable(false)
      Property<List<SubmittedFormValue>> submittedForms();

      void submittedForm( @Optional DomainEvent event, SubmittedFormValue form );
   }

   abstract class Mixin
         implements SubmittedForms, Data
   {
      @Structure
      Module module;

      @This
      Data state;

      @This
      FormDrafts submissions;

      @This
      FormAttachments formAttachments;

      public void submitForm( FormDraft formSubmission, Submitter submitter )
      {
         FormDraftDTO DTO = formSubmission.getFormDraftValue();
         
         ValueBuilder<SubmittedFormValue> formBuilder = module.valueBuilderFactory().newValueBuilder(SubmittedFormValue.class);

         formBuilder.prototype().submitter().set( EntityReference.getEntityReference(submitter) );
         formBuilder.prototype().form().set( DTO.form().get() );
         formBuilder.prototype().submissionDate().set( new Date() );

         // Check for signatures
         RequiredSignatures.Data requiredSignatures = module.unitOfWorkFactory().currentUnitOfWork().get( RequiredSignatures.Data.class, DTO.form().get().identity() );
         if (!requiredSignatures.requiredSignatures().get().isEmpty())
         {
            if (requiredSignatures.requiredSignatures().get().size() != DTO.signatures().get().size())
            {
               throw new IllegalArgumentException( "signatures_missing" );
            }
         }
         
         ValueBuilder<SubmittedFieldValue> fieldBuilder = module.valueBuilderFactory().newValueBuilder(SubmittedFieldValue.class);
         for (PageSubmissionDTO pageDTO : DTO.pages().get())
         {
            ValueBuilder<SubmittedPageValue> pageBuilder = module.valueBuilderFactory().newValueBuilder(SubmittedPageValue.class);
            pageBuilder.prototype().page().set(pageDTO.page().get());

            for (FieldSubmissionDTO field : pageDTO.fields().get())
            {
               // ignore comment fields when submitting
               if ( !(field.field().get().fieldValue().get() instanceof CommentFieldValue) )
               {
                  // Is mandatory field missing?
                  if (field.field().get().mandatory().get() && Strings.empty( field.value().get() ))
                     throw new IllegalArgumentException( "mandatory_value_missing" );
                  // Validate
                  if (field.field().get() != null && field.value().get() != null && !field.field().get().fieldValue().get().validate( field.value().get() ))
                     throw new IllegalArgumentException( "invalid_value" );

                  if ( field.field().get().field().get().identity().contains( "_" ) )
                  {
                     // this is a field of a field group. The entity id need to be fixed
                     // back from the change done in FormDrafts.createDraft
                     String concatenated = field.field().get().field().get().identity();
                     String fixed = concatenated.substring( concatenated.indexOf( "_" ) + 1 );
                     fieldBuilder.prototype().field().set( EntityReference.parseEntityReference( fixed ) );
                  } else
                  {
                     fieldBuilder.prototype().field().set( field.field().get().field().get() );
                  }

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
                        AttachmentFieldSubmission currentFormDraftAttachmentField = module.valueBuilderFactory().newValueFromJSON(AttachmentFieldSubmission.class, fieldBuilder.prototype().value().get());
                        AttachmentEntity attachment = module.unitOfWorkFactory().currentUnitOfWork().get( AttachmentEntity.class, currentFormDraftAttachmentField.attachment().get().identity() );
                        ((FormAttachments)formSubmission).moveAttachment( formAttachments, attachment );
                     } catch (ConstructionException e)
                     {
                        // ignore
                     }
                  }
                  pageBuilder.prototype().fields().get().add( fieldBuilder.newInstance() );
               }
            }

            formBuilder.prototype().pages().get().add(pageBuilder.newInstance());
         }

         // Transfer signatures
         formBuilder.prototype().signatures().get().addAll(DTO.signatures().get());

         submittedForm( null, formBuilder.newInstance() );

         // Now discard it
         submissions.discardFormDraft( formSubmission );
      }


      public void submittedForm( @Optional DomainEvent event, SubmittedFormValue form )
      {
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
