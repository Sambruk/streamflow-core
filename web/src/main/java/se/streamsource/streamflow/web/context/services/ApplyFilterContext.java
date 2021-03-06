/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
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
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.util.Visitor;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.application.mail.HtmlMailGenerator;
import se.streamsource.streamflow.web.application.mail.MailSender;
import se.streamsource.streamflow.web.application.security.UserPrincipal;
import se.streamsource.streamflow.web.context.workspace.cases.CaseCommandsContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.conversation.MessageDraft;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.group.Participants;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
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
 *
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
   private SystemDefaultsService systemDefaults;

   public ApplyFilterContext(@Structure Module module, @Uses MailSender mailSender, @Service AttachmentStore attachmentStore, @Service SystemDefaultsService systemDefaults)
   {
      this.module = module;
      this.mailSender = mailSender;
      this.attachmentStore = attachmentStore;
      this.filterCheck = new FilterCheck();
      this.filterable = new Filterable();
      this.htmlGenerator = module.objectBuilderFactory().newObject( HtmlMailGenerator.class );
      this.bundle = ResourceBundle.getBundle(ApplyFilterContext.class.getName(), new Locale("SV", "se")); // TODO These texts need to use the locale for the user
      this.systemDefaults = systemDefaults;
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
            RoleMap.current().set( module.unitOfWorkFactory().currentUnitOfWork().get( UserAuthentication.class, UserEntity.ADMINISTRATOR_USERNAME ) );
            RoleMap.current().set( new UserPrincipal( UserEntity.ADMINISTRATOR_USERNAME ) );
            
            /*
             * Have commented this piece of code since it's not available to the user in the adminview.
             * They where implemented from start by Rickard but have never been in use and has not been tested.
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

            RoleMap.current().set(self);
            
            if (actionValue instanceof CloseActionValue)
            {

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

               createConversationMessageWithAttachments( RoleMap.role( UserEntity.class ), participant );

            } else if (actionValue instanceof EmailNotificationActionValue)
            {
               Participant participant = module.unitOfWorkFactory().currentUnitOfWork().get(Participant.class, ((EmailNotificationActionValue) actionValue).participant().get().identity());

               createNotificationConversation( RoleMap.role( UserEntity.class ), participant );
            }
         }
      }

      private void createConversationMessageWithAttachments(UserEntity administrator, Participant participant)
      {
         Conversations conversations = RoleMap.role( Conversations.class );
         Conversation conversation = conversations.createConversation( bundle.getString( "subject" ) + self.caseId().get(), administrator );
         if( participant instanceof ConversationParticipant )
         {
            conversation.addParticipant( (ConversationParticipant)participant );
         } else if (participant instanceof Participants)
         {
            Participants.Data participants = (Participants.Data)participant;
            for (Participant participant1 : participants.participants())
            {
               conversation.addParticipant( (ConversationParticipant)participant1 );
            }
         }

         ((MessageDraft)conversation).changeDraftMessage(  bundle.getString( "message" ));

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

            Attachment conversationAttachment = conversation.createAttachment( "store:" + id );
            conversationAttachment.changeDescription( self.caseId().get() + ".pdf" );
            conversationAttachment.changeMimeType( "application/pdf" );
            conversationAttachment.changeModificationDate( self.createdOn().get() );
            conversationAttachment.changeSize( attachmentStore.getAttachmentSize( id ) );
            conversationAttachment.changeName( self.caseId().get() + ".pdf" );


            if ( self.attachments().count() > 0 ) {
               for (Attachment caseAttachment : self.attachments())
               {
                  conversation.addAttachment( caseAttachment );
               }
            }

            if( self.formAttachments().count() > 0 )
            {
               for( Attachment formAttachment : self.formAttachments())
               {
                  conversation.addAttachment( formAttachment );
               }
            }

         } catch (Throwable throwable)
         {
            logger.error("Could not create case notification message.", throwable);
         }
         conversation.createMessageFromDraft( administrator, MessageType.HTML );
      }

      private void createNotificationConversation(UserEntity administrator, Participant participant)
      {
         Conversations conversations = RoleMap.role( Conversations.class );
         Conversation conversation = conversations.createConversation( bundle.getString( "subject" ) + self.caseId().get(), administrator );
         if( participant instanceof ConversationParticipant )
         {
            conversation.addParticipant( (ConversationParticipant)participant );
         } else if (participant instanceof Participants)
         {
            Participants.Data participants = (Participants.Data)participant;
            for (Participant participant1 : participants.participants())
            {
              conversation.addParticipant( (ConversationParticipant)participant1 );
            }
         }

         StringBuffer notification = new StringBuffer();
         SimpleDateFormat dateFormat = new SimpleDateFormat( bundle.getString( "date_format" ) );
         notification.append( bundle.getString( "description" )).append(": ").append(self.getDescription()).append("<BR>");
         if (self.casepriority().get() != null)
         {
            notification.append( bundle.getString( "priority" )).append(": ").append(self.casepriority().get().getDescription()).append("<BR>");
         }
         notification.append( bundle.getString( "createdon" )).append(": ").append( dateFormat.format( self.createdOn().get() )).append("<BR>");
         if (self.dueOn().get() != null)
         {
            notification.append( bundle.getString( "duedate" )).append(": ").append( dateFormat.format( self.dueOn().get())).append("<BR>");
         }
         if (self.createdBy().get() instanceof User)
         {
            notification.append( bundle.getString( "createdby" )).append(": ").append(((Describable)self.createdBy().get()).getDescription()).append("<BR>");
         }
         if( self.owner().get() != null )
         {
            notification.append( bundle.getString( "owner" )).append(": ").append(((Describable)self.owner().get()).getDescription()).append("<BR>");
         }
         if( self.caseType().get() != null )
         {
            notification.append( bundle.getString( "casetype" )).append(": ").append(self.caseType().get().getDescription()).append("<BR>");
         }

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
         notification.append( "<BR>" );

         // add link to case for the possibility to open by webclient
         notification.append( "<a href='" );
         notification.append( systemDefaults.config().configuration().webclientBaseUrl() );
         notification.append( self.identity().get() );
         notification.append( "'>" );
         notification.append( bundle.getString( "openlink" ) );
         notification.append( "</a>");

         notification.append( "<br>" );

         ((MessageDraft)conversation).changeDraftMessage(notification.toString());

         conversation.createMessageFromDraft( administrator, MessageType.HTML );
      }
   }
}