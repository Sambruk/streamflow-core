/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.CommentFieldValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary.SurfaceSummaryContext;
import se.streamsource.streamflow.web.domain.entity.attachment.AttachmentEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.attachment.FormAttachments;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.task.DoubleSignatureTask;
import se.streamsource.streamflow.web.domain.structure.task.DoubleSignatureTasks;
import se.streamsource.streamflow.web.rest.service.mail.MailSenderService;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * Maintains list of submitted forms on a case
 */
@Concerns(SubmittedForms.DoubleSignatureTaskCreationConcern.class)
@Mixins(SubmittedForms.Mixin.class)
public interface SubmittedForms
{
   SubmittedFormValue submitForm( FormDraft formSubmission, Submitter submitter );

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

   }

   interface Events
   {
      void submittedForm( @Optional DomainEvent event, SubmittedFormValue form );
   }

   abstract class Mixin
         implements SubmittedForms, Events
   {
      @Structure
      Module module;

      @This
      Data state;

      @This
      FormDrafts submissions;

      @This
      FormAttachments formAttachments;

      public SubmittedFormValue submitForm( FormDraft formSubmission, Submitter submitter )
      {
         FormDraftDTO DTO = formSubmission.getFormDraftValue();
         
         ValueBuilder<SubmittedFormValue> formBuilder = module.valueBuilderFactory().newValueBuilder(SubmittedFormValue.class);

         formBuilder.prototype().submitter().set( EntityReference.getEntityReference(submitter) );
         formBuilder.prototype().form().set( DTO.form().get() );
         formBuilder.prototype().submissionDate().set( new Date() );

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

         // Check for active signatures, catch and ignore IllegalArgumentException if we do not have a role AccessPoint
         // in that case we are coming from the clients form wizard!!
         AccessPoint accessPoint = null;
         try
         {
            accessPoint= role( AccessPoint.class );
         } catch( IllegalArgumentException ia )
         {
            // do nothing - this approach is used to determine if we are coming from Surface Webforms or from client form wizard.
         }
         if( accessPoint != null )
         {
            RequiredSignatures.Data requiredSignatures = module.unitOfWorkFactory().currentUnitOfWork().get( RequiredSignatures.Data.class, ((Identity) accessPoint).identity().get() );
            Iterable<RequiredSignatureValue> activeSignatures = Iterables.filter( new Specification<RequiredSignatureValue>()
            {
               public boolean satisfiedBy( RequiredSignatureValue signature )
               {
                  return signature.active().get();
               }
            }, requiredSignatures.requiredSignatures().get() );

            // set second signee if we expect one
            if ( Iterables.count( activeSignatures ) > 1 )
            {
               formBuilder.prototype().secondsignee().set( DTO.secondsignee().get() );
            }

            // Transfer signatures
            formBuilder.prototype().signatures().get().addAll(DTO.signatures().get());
         }

         SubmittedFormValue submittedForm = formBuilder.newInstance();
         submittedForm( null, submittedForm );
         // Now discard it
         submissions.discardFormDraft( formSubmission );

         return submittedForm;
      }

      public void submittedForm( @Optional DomainEvent event, SubmittedFormValue form )
      {
         List<SubmittedFormValue> forms = state.submittedForms().get();
         forms.add( form );
         state.submittedForms().set( forms );
      }

      public boolean hasSubmittedForms()
      {
         return !state.submittedForms().get().isEmpty();
      }

   }

   abstract class DoubleSignatureTaskCreationConcern
      extends ConcernOf<SubmittedForms>
      implements SubmittedForms
   {
      @Structure
      Module module;

      @This
      CaseId.Data caseId;

      @This
      SubmittedForms forms;

      @Optional
      @Service
      MailSenderService mailSender;

      @Optional
      @Service
      SystemDefaultsService defaults;

      final Logger logger = LoggerFactory.getLogger( SubmittedForms.class.getName() );

      @This
      FormDrafts submissions;


      public SubmittedFormValue submitForm( FormDraft formSubmission, Submitter submitter )
      {
         SubmittedFormValue submittedForm = next.submitForm( formSubmission, submitter );

         // Check for active signatures, catch and ignore IllegalArgumentException if we do not have a role AccessPoint
         // in that case we are coming from the clients form wizard!!
         AccessPoint accessPoint = null;
         try
         {
            accessPoint = role( AccessPoint.class );
         } catch (IllegalArgumentException ia)
         {
            // do nothing - this approach is used to determine if we are coming from Surface Webforms or from client form wizard.
         }
         if (accessPoint != null)
         {
            RequiredSignatures.Data requiredSignatures = module.unitOfWorkFactory().currentUnitOfWork().get( RequiredSignatures.Data.class, ((Identity) accessPoint).identity().get() );
            Iterable<RequiredSignatureValue> activeSignatures = Iterables.filter( new Specification<RequiredSignatureValue>()
            {
               public boolean satisfiedBy( RequiredSignatureValue signature )
               {
                  return signature.active().get();
               }
            }, requiredSignatures.requiredSignatures().get() );

            // set second signee if we expect one
            if (Iterables.count( activeSignatures ) > 1)
            {
               if (submittedForm.secondsignee().get() != null && !submittedForm.secondsignee().get().singlesignature().get())
               {
                  DoubleSignatureTasks signatureTasks = role( DoubleSignatureTasks.class );
                  DoubleSignatureTask task = signatureTasks.createTask( role( Case.class ), submittedForm, null );

                  Form secondForm = module.unitOfWorkFactory().currentUnitOfWork().get( Form.class, requiredSignatures.requiredSignatures().get().get( 1 ).formid().get() );

                  FormDraft draft = submissions.createFormDraft( secondForm );
                  task.updateFormDraft( draft );

                  ResourceBundle bundle = ResourceBundle.getBundle( SurfaceSummaryContext.class.getName(), role( Locale.class ) );

                  try
                  {
                     ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder( EmailValue.class );

                     builder.prototype().subject().set( bundle.getString( "signature_notification_subject" ) );
                     builder.prototype().content().set( MessageFormat.format( bundle.getString( "signature_notification_body" ),
                           caseId.caseId().get(), defaults.config().configuration().webFormsProxyUrl().get() + "/cases/" + caseId.caseId().get()
                           + "/formdrafts/" + ((Identity) draft).identity().get() + "/index" ) );
                     builder.prototype().contentType().set( "text/plain" );
                     builder.prototype().to().set( submittedForm.secondsignee().get().email().get() );

                     mailSender.sentEmail( builder.newInstance() );

                  } catch (Throwable throwable)
                  {
                     logger.error( "Could not send mail", throwable );
                  }
               }
            }
         }
         return submittedForm;
      }
   }
}
