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

import static se.streamsource.dci.api.RoleMap.role;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.velocity.VelocityContext;
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
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.util.MessageTemplate;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.util.Visitor;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.HtmlMailGenerator;
import se.streamsource.streamflow.web.application.pdf.PdfGeneratorService;
import se.streamsource.streamflow.web.domain.entity.attachment.AttachmentEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.user.EmailUserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.DefaultPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.FormPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
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
import se.streamsource.streamflow.web.domain.structure.user.Users;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;
import se.streamsource.streamflow.web.infrastructure.attachment.OutputstreamInput;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountFormSummaryConcern.class)
@Mixins(SurfaceSummaryContext.Mixin.class)
public interface SurfaceSummaryContext
      extends Context, IndexContext<FormDraftDTO>
{

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
      SystemDefaultsService defaults;

      @Structure
      ValueBuilderFactory vbf;

      @Service
      AttachmentStore attachmentStore;

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
         Users users = RoleMap.role( Users.class );
         AccessPoint accessPoint = role( AccessPoint.class );

         UserEntity administrator = module.unitOfWorkFactory().currentUnitOfWork().get(UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME);


         SubmittedFormValue submittedForm = userCases.submitFormAndSendCase( aCase, formSubmission, user );
         DoubleSignatureTask task = createDoubleSignatureTaskIfNeccessary( aCase, submittedForm );
         if( task != null )
         {
            // set task reference back to subittedform - second signee info

            try
            {

               Organizations.Data organizations = module.unitOfWorkFactory().currentUnitOfWork().get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID );
               String organisation = organizations.organization().get().getDescription();
               String id = ((CaseId.Data)aCase).caseId().get();
               String link = defaults.config().configuration().webFormsProxyUrl().get() + "?tid=" + ((Identity)task ).identity().get();

               VelocityContext context = new VelocityContext();
               context.put( "organisation", organisation );
               context.put( "id", id );
               context.put( "link", link );

               String subjectText = MessageTemplate.text( accessPoint.subject().get() )
                     .bind( "caseId", id ).bind("organisation", organisation).eval();

               String velocityTemplate = accessPoint.emailTemplates().get().get( "secondsigneenotification" );
               String htmlMail = module.objectBuilderFactory().newObject( HtmlMailGenerator.class ).createDoubleSignatureMail( velocityTemplate, context );

               Conversations conversations = RoleMap.role( Conversations.class );
               Conversation conversation = conversations.createConversation( subjectText, administrator );
               EmailUserEntity emailUser = users.createEmailUser( submittedForm.secondsignee().get().email().get() );
               conversation.addParticipant( emailUser );

               conversation.createMessage( htmlMail, MessageType.HTML, administrator );

               // TODO is there a way to collect the email value created by notification service to save into the task
               // for resend
               ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder( EmailValue.class );

               builder.prototype().subject().set( subjectText );
               builder.prototype().contentType().set( Translator.HTML );
               builder.prototype().content().set( htmlMail );
               builder.prototype().to().set( submittedForm.secondsignee().get().email().get() );

               EmailValue email = builder.newInstance();
               task.updateEmailValue( email );
               task.updateSecondDraftUrl( link );
               task.updateLastReminderSent(new DateTime( DateTimeZone.UTC ) );

            } catch (Throwable throwable)
            {
               logger.error( "Could not create message", throwable );
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
                  Conversations conversations = RoleMap.role( Conversations.class );
                  Conversation conversation = conversations.createConversation( accessPoint.getDescription(), user );

                  PDDocument document = generatePdf( submittedFormValue );
                  String attachmentStoreId = addToAttachmentStore( document );
                  Attachment formPdfAttachment = conversation.createAttachment( "store:" + attachmentStoreId );
                  formPdfAttachment.changeDescription( accessPoint.getDescription() + ".pdf" );
                  formPdfAttachment.changeMimeType( "application/pdf" );
                  formPdfAttachment.changeModificationDate( new Date( ) );
                  formPdfAttachment.changeSize( attachmentStore.getAttachmentSize( attachmentStoreId ) );
                  formPdfAttachment.changeName( accessPoint.getDescription() + ".pdf" );

                  // find all form attachments and attach them to the email as well
                  List<AttachedFileValue> formAttachments = new ArrayList<AttachedFileValue>();
                  for (SubmittedFieldValue value : submittedFormValue.fields())
                  {
                     FieldValueDefinition.Data field = module.unitOfWorkFactory().currentUnitOfWork().get( FieldValueDefinition.Data.class, value.field().get().identity() );
                     if ( field.fieldValue().get() instanceof AttachmentFieldValue)
                     {
                        if ( !Strings.empty( value.value().get() ) )
                        {
                           AttachmentFieldSubmission currentFormDraftAttachmentField = module.valueBuilderFactory().newValueFromJSON(AttachmentFieldSubmission.class, value.value().get() );
                           AttachmentEntity attachment = module.unitOfWorkFactory().currentUnitOfWork().get( AttachmentEntity.class, currentFormDraftAttachmentField.attachment().get().identity() );
                           conversation.addAttachment( attachment );

                        }
                     }
                  }

                  String[] mailAddresses = form.enteredEmails().get().split( "," );

                  for( String mailAddress : mailAddresses )
                  {
                     conversation.addParticipant( users.createEmailUser( mailAddress ) );
                  }

                  ResourceBundle bundle = ResourceBundle.getBundle( SurfaceSummaryContext.class.getName(), locale );
                  HtmlMailGenerator htmlMailGenerator = module.objectBuilderFactory().newObject( HtmlMailGenerator.class );

                  conversation.changeDraftMessage( bundle.getString( "mail_notification_body" ) );

                  conversation.createMessageFromDraft( administrator, MessageType.HTML );
               }
            } catch (Throwable throwable)
            {
               logger.error( "Could not create confirmation conversation message.", throwable );
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