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
package se.streamsource.streamflow.web.context.services;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.Role;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.administration.filter.ActionValue;
import se.streamsource.streamflow.api.administration.filter.CloseActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailNotificationActionValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.api.administration.filter.LabelRuleValue;
import se.streamsource.streamflow.api.administration.filter.RuleValue;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.util.Visitor;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.HtmlMailGenerator;
import se.streamsource.streamflow.web.application.mail.MailSender;
import se.streamsource.streamflow.web.context.workspace.cases.CaseCommandsContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.group.Participants;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;
import se.streamsource.streamflow.web.infrastructure.attachment.OutputstreamInput;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static se.streamsource.streamflow.util.ForEach.*;

/**
 * TODO
 */
public class ApplyFilterContext
{
   private FilterCheck filterCheck;
   private Filterable filterable;
   private Module module;
   private MailSender mailSender;
   private AttachmentStore attachmentStore;
   private ResourceBundle bundle;
   private HtmlMailGenerator htmlGenerator;

   public ApplyFilterContext(@Structure Module module, @Uses MailSender mailSender, @Service AttachmentStore attachmentStore)
   {
      this.module = module;
      this.mailSender = mailSender;
      this.attachmentStore = attachmentStore;
      this.filterCheck = new FilterCheck();
      this.filterable = new Filterable();
      this.htmlGenerator = module.objectBuilderFactory().newObject( HtmlMailGenerator.class );
      this.bundle = ResourceBundle.getBundle( ApplyFilterContext.class.getName(), new Locale("SV","se") ); // TODO These texts need to use the locale for the user
   }

   public ApplyFilterContext rebind(Filters.Data filters, CaseEntity caze)
   {
      filterCheck.bind(filters);
      filterable.bind(caze);
      return this;
   }

   public void applyFilters()
   {
      filterCheck.applyFilters(filterable);
   }

   private class FilterCheck
         extends Role<Filters.Data>
   {
      public void applyFilters(final Filterable filterable)
      {
         for (FilterValue filterValue : forEach(self.filters().get()).
               filter(enabledFilter()).
               filter(filterTrigger().map(filterable)))
         {
            filterable.applyActions(filterValue.actions().get());
         }
      }

      private Specification<FilterValue> enabledFilter()
      {
         return new Specification<FilterValue>()
         {
            public boolean satisfiedBy(FilterValue item)
            {
               return item.enabled().get();
            }
         };
      }

      private Function<Filterable, Specification<FilterValue>> filterTrigger()
      {
         return new Function<Filterable, Specification<FilterValue>>()
         {
            public Specification<FilterValue> map(final Filterable filterable)
            {
               return new Specification<FilterValue>()
               {
                  public boolean satisfiedBy(FilterValue item)
                  {
                     List<Specification<Filterable>> specs = Iterables.addAll(new ArrayList<Specification<Filterable>>(), Iterables.map(new RuleFunction(), item.rules().get()));
                     Specification<Filterable>[] specifications = specs.toArray((Specification<Filterable>[]) new Specification[specs.size()]);

                     Specification<Filterable> specification;
                     if (item.matching().get().equals(FilterValue.MatchingEnum.all))
                     {
                        specification = Specifications.and(specifications);
                     } else
                        specification = Specifications.or(specifications);

                     return specification.satisfiedBy(filterable);
                  }
               };
            }
         };
      }

      class RuleFunction
            implements Function<RuleValue, Specification<Filterable>>
      {
         public Specification<Filterable> map(final RuleValue ruleValue)
         {
            if (ruleValue instanceof LabelRuleValue)
            {
               return new Specification<Filterable>()
               {
                  public boolean satisfiedBy(Filterable item)
                  {
                     return item.hasLabel(((LabelRuleValue) ruleValue).label().get().identity());
                  }
               };
            }

            return null;
         }
      }
   }

   private class Filterable
         extends Role<CaseEntity>
   {
      Logger logger = LoggerFactory.getLogger(ApplyFilterContext.class);

      private Filterable()
      {
      }

      public boolean hasLabel(String identity)
      {
         for (Label label : self.labels())
         {
            if (label.toString().equals(identity))
               return true;
         }
         return false;
      }

      public void applyActions(Iterable<ActionValue> actions)
      {
         for (ActionValue actionValue : actions)
         {
            // Run as Administrator
            RoleMap.newCurrentRoleMap();
            UserEntity administrator = module.unitOfWorkFactory().currentUnitOfWork().get(UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME);
            RoleMap.current().set(administrator);
            
            /*
             * Have commented this piece of code since it's not available to the user in the adminview.
             * They where inplemented from start by Rickard but have never been in use and has not been tested.
             * The usecase are rather complex and reuires some thinking in order to not mess up...
             * 
            if (actionValue instanceof AssignActionValue)
            {
               Assignee assignee = module.unitOfWorkFactory().currentUnitOfWork().get(Assignee.class, ((AssignActionValue) actionValue).assignee().get().identity());

               RoleMap.current().set(assignee);
               RoleMap.current().set(self);

               try
               {
                  module.transientBuilderFactory().newTransient(CaseCommandsContext.class).assign();
               } catch (Throwable e)
               {
                  throw new IllegalStateException("Could not assign", e);
               }

               logger.info("Assigned " + self.caseId().get() + " to " + ((Describable) assignee).getDescription());
            } else if (actionValue instanceof ChangeOwnerActionValue)
            {
               Owner owner = module.unitOfWorkFactory().currentUnitOfWork().get(Owner.class, ((ChangeOwnerActionValue) actionValue).sendTo().get().identity());

               RoleMap.current().set(self);

               try
               {
                  EntityValue to = module.valueBuilderFactory().newValueBuilder(EntityValue.class).prototype();
                  to.entity().set(((Identity) owner).identity().get());
                  module.transientBuilderFactory().newTransient(CaseCommandsContext.class).sendto(to);
               } catch (Throwable e)
               {
                  throw new IllegalStateException("Could not change owner", e);
               }

               logger.info("Changed owner of " + self.caseId().get() + " to " + ((Describable) owner).getDescription());
              
            } else 
             */
            if (actionValue instanceof CloseActionValue)
            {
               RoleMap.current().set(self);

               try
               {
                  module.transientBuilderFactory().newTransient(CaseCommandsContext.class).close();
               } catch (Throwable e)
               {
                  throw new IllegalStateException("Could not close", e);
               }

               logger.info("Closed" + self.caseId().get());
            } else if (actionValue instanceof EmailActionValue)
            {
               Participant participant = module.unitOfWorkFactory().currentUnitOfWork().get(Participant.class, ((EmailActionValue) actionValue).participant().get().identity());

               sendEmailToParticipant(administrator, participant);
            } else if (actionValue instanceof EmailNotificationActionValue)
            {
               Participant participant = module.unitOfWorkFactory().currentUnitOfWork().get(Participant.class, ((EmailNotificationActionValue) actionValue).participant().get().identity());

               sendEmailNotificationToParticipant(administrator, participant);
            }
         }
      }

      private void sendEmailToParticipant(UserEntity administrator, Participant participant)
      {
         //TODO Create conversation for this message so we actually are able to receive responses to this mail
         if (participant instanceof Contactable)
         {
            Contactable contact = (Contactable) participant;
            ContactEmailDTO email = contact.getContact().defaultEmail();
            if (email != null)
            {
               ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder(EmailValue.class);

               // leave from address empty to allow mail sender to pick up
               // the default mail address from mail sender configuration
               builder.prototype().fromName().set(((Describable) self.owner().get()).getDescription());
               builder.prototype().subject().set(bundle.getString( "subject" ) + self.caseId().get()); 
               builder.prototype().content().set( htmlGenerator.createMailContent( bundle.getString( "message" ), "" ) );
               builder.prototype().contentType().set( Translator.HTML );
               builder.prototype().to().set(email.emailAddress().get());

               try
               {
                  // Store case as PDF for attachment purposes
                  ValueBuilder<CaseOutputConfigDTO> config = module.valueBuilderFactory().newValueBuilder(CaseOutputConfigDTO.class);
                  config.prototype().attachments().set(true);
                  config.prototype().contacts().set(true);
                  config.prototype().conversations().set(true);
                  config.prototype().submittedForms().set(true);
                  config.prototype().caselog().set(true);
                  RoleMap.current().set(new Locale( "SV", "se" ));
                  RoleMap.current().set(self);
                  final PDDocument pdf = module.transientBuilderFactory().newTransient(CaseCommandsContext.class).exportpdf(config.newInstance());

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

                  System.out.println("Written to:" + id + ", length:" + attachmentStore.getAttachmentSize(id));

                  List<AttachedFileValue> attachments = builder.prototype().attachments().get();
                  ValueBuilder<AttachedFileValue> attachment = module.valueBuilderFactory().newValueBuilder(AttachedFileValue.class);
                  attachment.prototype().mimeType().set("application/pdf");
                  attachment.prototype().uri().set("store:" + id);
                  attachment.prototype().modificationDate().set(self.createdOn().get());
                  attachment.prototype().name().set(self.caseId().get() + ".pdf");
                  attachment.prototype().size().set(attachmentStore.getAttachmentSize(id));
                  attachments.add(attachment.newInstance());
                  
                  
                  if ( self.attachments().count() > 0 ) {
                     for (Attachment caseAttachment : self.attachments())
                     {
                        AttachedFile.Data attachedFile = (AttachedFile.Data) caseAttachment;
                        attachment.prototype().mimeType().set(attachedFile.mimeType().get());
                        attachment.prototype().uri().set(attachedFile.uri().get());
                        attachment.prototype().modificationDate().set(attachedFile.modificationDate().get());
                        attachment.prototype().name().set(attachedFile.name().get());
                        attachment.prototype().size().set(attachedFile.size().get());
                        attachments.add( attachment.newInstance() );
                     }
                  }

                  if( self.formAttachments().count() > 0 )
                  {
                     for( Attachment formAttachment : self.formAttachments())
                     {
                        AttachedFile.Data attachedFile = (AttachedFile.Data) formAttachment;
                        attachment.prototype().mimeType().set(attachedFile.mimeType().get());
                        attachment.prototype().uri().set(attachedFile.uri().get());
                        attachment.prototype().modificationDate().set(attachedFile.modificationDate().get());
                        attachment.prototype().name().set(attachedFile.name().get());
                        attachment.prototype().size().set(attachmentStore.getAttachmentSize( attachedFile.uri().get() ));
                        attachments.add( attachment.newInstance() );
                     }
                  }

                  mailSender.sentEmail(null, builder.newInstance());

                  logger.info("Emailed " + self.caseId().get() + " to " + contact.getContact().name().get() + "(" + email.emailAddress().get() + ")");
               } catch (Throwable throwable)
               {
                  logger.error("Could not email case to " + email.emailAddress().get(), throwable);
               }
            }
         } else if (participant instanceof Participants)
         {
            Participants.Data participants = (Participants.Data)participant;
            for (Participant participant1 : participants.participants())
            {
               sendEmailToParticipant(administrator, participant1);
            }
         }

      }
      
      private void sendEmailNotificationToParticipant(UserEntity administrator, Participant participant)
      {
         //TODO Create conversation for this message so we actually are able to receive responses to this mail
         if (participant instanceof Contactable)
         {
            Contactable contact = (Contactable) participant;
            ContactEmailDTO email = contact.getContact().defaultEmail();
            if (email != null)
            {
               ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder(EmailValue.class);

               // leave from address empty to allow mail sender to pick up
               // the default mail address from mail sender configuration
               builder.prototype().fromName().set(((Describable) self.owner().get()).getDescription());
               builder.prototype().subject().set(bundle.getString( "subject" ) + self.caseId().get()); 
               builder.prototype().contentType().set( Translator.HTML );
               builder.prototype().to().set(email.emailAddress().get());
               StringBuffer notification = new StringBuffer();
               SimpleDateFormat dateFormat = new SimpleDateFormat( bundle.getString( "date_format" ) );
               notification.append( bundle.getString( "description" )).append(": ").append(self.getDescription()).append("\n");
               if (self.priority().get() != null)
               {
                  notification.append( bundle.getString( "priority" )).append(": ").append(self.priority().get().getDescription()).append("\n");
               }
               notification.append( bundle.getString( "createdon" )).append(": ").append( dateFormat.format( self.createdOn().get() )).append("\n");
               if (self.dueOn().get() != null)
               {
                  notification.append( bundle.getString( "duedate" )).append(": ").append( dateFormat.format( self.dueOn().get())).append("\n");
               }
               if (self.createdBy().get() instanceof User)
               {
                  notification.append( bundle.getString( "createdby" )).append(": ").append(((Describable)self.createdBy().get()).getDescription()).append("\n");
               }   
               notification.append( bundle.getString( "owner" )).append(": ").append(((Describable)self.owner().get()).getDescription()).append("\n"); 
               notification.append( bundle.getString( "casetype" )).append(": ").append(self.caseType().get().getDescription()).append("\n");
               notification.append( bundle.getString( "labels" )).append(": ");
               boolean first = true;
               for (Label label : self.labels())
               {
                  if (!first) {
                     notification.append(", ");
                  }
                  notification.append(label.getDescription());
                  first = false;
               }
                                             
               builder.prototype().content().set( htmlGenerator.createMailContent( notification.toString(), "" ) );

               try {
                  mailSender.sentEmail(null, builder.newInstance());

                  logger.info("Emailed notification for " + self.caseId().get() + " to " + contact.getContact().name().get() + "(" + email.emailAddress().get() + ")");
               } catch (Throwable throwable)
               {
                  logger.error("Could not email notification of case to " + email.emailAddress().get(), throwable);
               }
            }
         } else if (participant instanceof Participants)
         {
            Participants.Data participants = (Participants.Data)participant;
            for (Participant participant1 : participants.participants())
            {
               sendEmailNotificationToParticipant(administrator, participant1);
            }
         }

      }
   }
}