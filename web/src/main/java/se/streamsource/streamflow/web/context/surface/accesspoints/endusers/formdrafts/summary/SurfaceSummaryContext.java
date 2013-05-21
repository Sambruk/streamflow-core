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
package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignaturesValue;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.util.Visitor;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.pdf.PdfGeneratorService;
import se.streamsource.streamflow.web.domain.entity.attachment.AttachmentEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.attachment.DefaultPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.FormPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.form.EndUserCases;
import se.streamsource.streamflow.web.domain.structure.form.FieldValueDefinition;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;
import se.streamsource.streamflow.web.domain.structure.form.MailSelectionMessage;
import se.streamsource.streamflow.web.domain.structure.form.RequiredSignatures;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.task.DoubleSignatureTask;
import se.streamsource.streamflow.web.domain.structure.task.DoubleSignatureTasks;
import se.streamsource.streamflow.web.domain.structure.user.EndUser;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;
import se.streamsource.streamflow.web.infrastructure.attachment.OutputstreamInput;
import se.streamsource.streamflow.web.rest.service.mail.MailSenderService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountFormSummaryConcern.class)
@Mixins(SurfaceSummaryContext.Mixin.class)
public interface SurfaceSummaryContext
      extends Context, IndexContext<FormDraftDTO>
{
   //void submit();

   void submitandsend();

   RequiredSignaturesValue signatures();

   StringValue mailselectionmessage();

   void enablemailmessage();

   void disablemailmessage();

   void changeemailstobenotified( StringValue message );

   abstract class Mixin
         implements SurfaceSummaryContext
   {
      @Structure
      Module module;

      @Service
      PdfGeneratorService pdfGenerator;

      @Uses
      Locale locale;

      @Optional
      @Service
      MailSenderService mailSender;

      @Optional
      @Service
      SystemDefaultsService defaults;

      @Structure
      ValueBuilderFactory vbf;

      @Service
      AttachmentStore attachmentStore;

      @Optional
      @Service
      VelocityEngine velocity;

      final Logger logger = LoggerFactory.getLogger( SubmittedForms.class.getName() );


      public FormDraftDTO index()
      {
         return RoleMap.role( FormDraftDTO.class );
      }

      public void submitandsend()
      {
         EndUserCases userCases = RoleMap.role( EndUserCases.class );
         EndUser user = RoleMap.role( EndUser.class );
         FormDraft formSubmission = RoleMap.role( FormDraft.class );
         Case aCase = RoleMap.role( Case.class );

         SubmittedFormValue submittedForm = userCases.submitFormAndSendCase( aCase, formSubmission, user );
         DoubleSignatureTask task = createDoubleSignatureTaskIfNeccessary( aCase, submittedForm );
         if( task != null )
         {
            // set task reference back to subittedform - second signee info
            ResourceBundle bundle = ResourceBundle.getBundle( SurfaceSummaryContext.class.getName(), new Locale("sv","SE") );

            try
            {
               Template textTemplate = velocity.getTemplate( "/se/streamsource/streamflow/web/context/surface/tasks/doublesignaturetextmail_sv.html" );
               Template htmlTemplate = velocity.getTemplate( "/se/streamsource/streamflow/web/context/surface/tasks/doublesignaturehtmlmail_sv.html" );


               Organizations.Data organizations = module.unitOfWorkFactory().currentUnitOfWork().get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID );
               String organisation = organizations.organization().get().getDescription();
               String id = ((CaseId.Data)aCase).caseId().get();
               String link = defaults.config().configuration().webFormsProxyUrl().get() + "?tid=" + ((Identity)task ).identity().get();
               String textMail = createFormatedMail( id, link, organisation, textTemplate );
               String htmlMail = createFormatedMail( id, link, organisation, htmlTemplate );

               ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder( EmailValue.class );

               builder.prototype().subject().set( organisation + " - " + bundle.getString( "signature_notification_subject" ) );
               builder.prototype().content().set( textMail );
               builder.prototype().contentType().set( "text/plain" );
               builder.prototype().contentHtml().set( htmlMail );
               builder.prototype().to().set( submittedForm.secondsignee().get().email().get() );

               EmailValue email = builder.newInstance();
               task.updateEmailValue( email );
               task.updateSecondDraftUrl( link );
               task.updateLastReminderSent(new DateTime( DateTimeZone.UTC ) );

               mailSender.sentEmail( email );

            } catch (Throwable throwable)
            {
               logger.error( "Could not send mail", throwable );
               throw new ResourceException( Status.SERVER_ERROR_INTERNAL, throwable );
            }
         }

         FormDraftDTO form = role( FormDraftDTO.class );

         if ( form.mailSelectionEnablement().get() != null && form.mailSelectionEnablement().get() )
         {
            try
            {
               SubmittedForms.Data data = RoleMap.role( SubmittedForms.Data.class );

               SubmittedFormValue submittedFormValue = null;
               for (SubmittedFormValue value : data.submittedForms().get())
               {

                  if (value.form().get().identity().equals( form.form().get().identity() ))
                  {
                     submittedFormValue = value;
                  }
               }
               if ( submittedFormValue != null )
               {
                  // find all form attachments and attach them to the email as well
                  List<AttachedFileValue> formAttachments = new ArrayList<AttachedFileValue>();
                  for (SubmittedFieldValue value : submittedFormValue.fields())
                  {
                     FieldValueDefinition.Data field = module.unitOfWorkFactory().currentUnitOfWork().get( FieldValueDefinition.Data.class, value.field().get().identity() );
                     if ( field.fieldValue().get() instanceof AttachmentFieldValue )
                     {
                        if ( !Strings.empty( value.value().get() ) )
                        {
                           AttachmentFieldSubmission currentFormDraftAttachmentField = module.valueBuilderFactory().newValueFromJSON(AttachmentFieldSubmission.class, value.value().get() );
                           AttachmentEntity attachment = module.unitOfWorkFactory().currentUnitOfWork().get( AttachmentEntity.class, currentFormDraftAttachmentField.attachment().get().identity() );

                           ValueBuilder<AttachedFileValue> formAttachment = module.valueBuilderFactory().newValueBuilder( AttachedFileValue.class );
                           formAttachment.prototype().mimeType().set( URLConnection.guessContentTypeFromName( currentFormDraftAttachmentField.name().get() ) );
                           formAttachment.prototype().uri().set( attachment.uri().get() );
                           formAttachment.prototype().modificationDate().set( attachment.modificationDate().get() );
                           formAttachment.prototype().name().set( currentFormDraftAttachmentField.name().get() );
                           formAttachment.prototype().size().set( attachmentStore.getAttachmentSize( attachment.uri().get() ) );
                           formAttachments.add( formAttachment.newInstance() );
                        }
                     }
                  }
                  notifyByMail( submittedFormValue, form.enteredEmails().get(), formAttachments );
               }
            } catch (Throwable throwable)
            {
               logger.error( "Could not send mail", throwable );
            }
         }
      }

      private DoubleSignatureTask createDoubleSignatureTaskIfNeccessary( Case aCase, SubmittedFormValue submittedForm )
      {
         DoubleSignatureTask task = null;
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
            RequiredSignatures.Data requiredSignatures = ( RequiredSignatures.Data )accessPoint;
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
                  task = ((DoubleSignatureTasks)aCase).createTask( role( Case.class ), submittedForm, null );

                  Form secondForm = module.unitOfWorkFactory().currentUnitOfWork().get( Form.class, requiredSignatures.requiredSignatures().get().get( 1 ).formid().get() );

                  FormDraft draft = ((FormDrafts)aCase).createFormDraft( secondForm );
                  task.updateFormDraft( draft );
                  task.updateAccessPoint( accessPoint );
               }
            }
         }
         return task;
      }

      private String createFormatedMail( String id, String link, String organisation, Template template)
      {
         VelocityContext context = new VelocityContext();
         
         context.put( "organisation", organisation );
         context.put( "id", id );
         context.put( "link", link );
         StringWriter writer = new StringWriter();
         try
         {
            template.merge( context, writer );

            return writer.toString();
         } catch (IOException e)
         {
            throw new IllegalArgumentException("Could not create html mail", e);
         }
      }

      public RequiredSignaturesValue signatures()
      {
         AccessPoint accessPoint = RoleMap.role( AccessPoint.class );

         RequiredSignatures.Data data = module.unitOfWorkFactory().currentUnitOfWork().get( RequiredSignatures.Data.class, ((Identity)accessPoint).identity( ).get());


         ValueBuilder<RequiredSignaturesValue> valueBuilder = module.valueBuilderFactory().newValueBuilder( RequiredSignaturesValue.class );
         valueBuilder.prototype().signatures().get();

         for (RequiredSignatureValue signature : data.requiredSignatures().get())
         {
            valueBuilder.prototype().signatures().get().add( signature );
         }
         return valueBuilder.newInstance();
      }

      public StringValue mailselectionmessage()
      {
         String message = RoleMap.current().get( MailSelectionMessage.Data.class ).mailSelectionMessage().get();
         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
         if ( message == null ) {
            message = "";
         }
         builder.prototype().string().set( message );
         return builder.newInstance();
      }


      public void enablemailmessage()
      {
         FormDraft formDraft = role( FormDraft.class );
         formDraft.enableEmailMessage();
      }

      public void disablemailmessage()
      {
         FormDraft formDraft = role( FormDraft.class );
         formDraft.disableEmailMessage();
      }

      public void changeemailstobenotified( StringValue message )
      {
         FormDraft formDraft = role( FormDraft.class );
         formDraft.changeEmailsToBeNotified( message );
      }

      private PDDocument generatePdf( SubmittedFormValue submittedFormValue ) throws Throwable
      {
         FormDraftDTO form = role( FormDraftDTO.class );

         FormPdfTemplate.Data selectedTemplate = role( FormPdfTemplate.Data.class);
         AttachedFile.Data template = (AttachedFile.Data) selectedTemplate.formPdfTemplate().get();

         if (template == null)
         {
            ProxyUser proxyUser = role(ProxyUser.class);
            template = (AttachedFile.Data) ((FormPdfTemplate.Data) proxyUser.organization().get()).formPdfTemplate().get();

            if( template == null)
            {
               template = (AttachedFile.Data) ((DefaultPdfTemplate.Data) proxyUser.organization().get()).defaultPdfTemplate().get();
            }
         }
         String uri = null;
         if (template != null)
         {
            uri = template.uri().get();
         }

         CaseId.Data idData = role( CaseId.Data.class);

         return pdfGenerator.generateSubmittedFormPdf( submittedFormValue, idData, uri, locale );
      }

      private void notifyByMail( SubmittedFormValue form, String emails, List<AttachedFileValue> formAttachments ) throws Throwable
      {
         String[] mails = emails.split( "," );
         PDDocument document = generatePdf( form );

         // TODO handle case attachments: also attach them to the mail
         AccessPoint role = role( AccessPoint.class );
         Date submittedOn = form.submissionDate().get();

         mailFormPDF( role.getDescription(), submittedOn, document, formAttachments, mails );
      }

      private void mailFormPDF( String accessPointName, Date submittedOn, PDDocument document, List<AttachedFileValue> formAttachments, String... recipients )
      {
         //TODO Create conversation for this message so we acctually are able to receive responses to this mail
         ResourceBundle bundle = ResourceBundle.getBundle( SurfaceSummaryContext.class.getName(), locale );

         try
         {
            String id = addToAttachmentStore( document );
            for (String recipient : recipients)
            {
               ValueBuilder<EmailValue> builder = vbf.newValueBuilder( EmailValue.class);

               // leave from address and fromName empty to allow mail sender to pick up
               // default values from mail sender configuration
               builder.prototype().subject().set( accessPointName );
               builder.prototype().content().set( bundle.getString( "mail_notification_body" ) );
               builder.prototype().contentType().set("text/plain");
               builder.prototype().to().set( recipient );

               List<AttachedFileValue> attachments = builder.prototype().attachments().get();
               ValueBuilder<AttachedFileValue> attachment = vbf.newValueBuilder( AttachedFileValue.class );
               attachment.prototype().mimeType().set("application/pdf");
               attachment.prototype().uri().set("store:" + id);
               attachment.prototype().modificationDate().set( submittedOn );
               attachment.prototype().name().set( accessPointName + ".pdf");
               attachment.prototype().size().set(attachmentStore.getAttachmentSize(id));
               attachments.add(attachment.newInstance());

               if ( formAttachments.size() > 0 ) {
                  for (AttachedFileValue formAttachment : formAttachments)
                  {
                     attachments.add( formAttachment );
                  }
               }
               mailSender.sentEmail( builder.newInstance() );
            }
         } catch (Throwable throwable)
         {
            logger.error( "Could not send mail", throwable );
         }
      }

      private String addToAttachmentStore( final PDDocument pdf ) throws Throwable
      {

         // Store case as PDF for attachment purposes
         ValueBuilder<CaseOutputConfigDTO> config = vbf.newValueBuilder( CaseOutputConfigDTO.class );
         config.prototype().attachments().set(true);
         config.prototype().contacts().set(true);
         config.prototype().conversations().set(true);
         config.prototype().submittedForms().set(true);
         config.prototype().caselog().set(true);
         RoleMap.current().set(new Locale( "sv", "SE" ));

         String id = attachmentStore.storeAttachment(new OutputstreamInput(new Visitor<OutputStream, IOException>()
         {
            public boolean visit(OutputStream out) throws IOException
            {
               COSWriter writer = new COSWriter(out);

               try
               {
                  writer.write(pdf);
               } catch (COSVisitorException e)
               {
                  throw new IOException(e);
               } finally
               {
                  writer.close();
               }

               return true;
            }
         }, 4096));
         pdf.close();

         return id;
      }

   }
}