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

package se.streamsource.streamflow.web.context.services;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.qi4j.api.entity.Identity;
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
import se.streamsource.dci.value.EntityValue;
import se.streamsource.streamflow.api.administration.filter.ActionValue;
import se.streamsource.streamflow.api.administration.filter.AssignActionValue;
import se.streamsource.streamflow.api.administration.filter.ChangeOwnerActionValue;
import se.streamsource.streamflow.api.administration.filter.CloseActionValue;
import se.streamsource.streamflow.api.administration.filter.EmailActionValue;
import se.streamsource.streamflow.api.administration.filter.FilterValue;
import se.streamsource.streamflow.api.administration.filter.LabelRuleValue;
import se.streamsource.streamflow.api.administration.filter.RuleValue;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.util.Visitor;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.MailSender;
import se.streamsource.streamflow.web.context.workspace.cases.CaseCommandsContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.group.Participants;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;
import se.streamsource.streamflow.web.infrastructure.attachment.OutputstreamInput;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static se.streamsource.streamflow.util.ForEach.forEach;

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

   public ApplyFilterContext(@Structure Module module, @Uses MailSender mailSender, @Service AttachmentStore attachmentStore)
   {
      this.module = module;
      this.mailSender = mailSender;
      this.attachmentStore = attachmentStore;
      this.filterCheck = new FilterCheck();
      this.filterable = new Filterable();
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
                  throw new IllegalStateException("Could not assign", e);
               }

               logger.info("Changed owner of " + self.caseId().get() + " to " + ((Describable) owner).getDescription());
            } else if (actionValue instanceof CloseActionValue)
            {
               RoleMap.current().set(self);

               try
               {
                  module.transientBuilderFactory().newTransient(CaseCommandsContext.class).close();
               } catch (Throwable e)
               {
                  throw new IllegalStateException("Could not assign", e);
               }

               logger.info("Closed" + self.caseId().get());
            } else if (actionValue instanceof EmailActionValue)
            {
               Participant participant = module.unitOfWorkFactory().currentUnitOfWork().get(Participant.class, ((EmailActionValue) actionValue).participant().get().identity());

               sendEmailToParticipant(administrator, participant);
            }
         }
      }

      private void sendEmailToParticipant(UserEntity administrator, Participant participant)
      {
         if (participant instanceof Contactable)
         {
            Contactable contact = (Contactable) participant;
            ContactEmailDTO email = contact.getContact().defaultEmail();
            if (email != null)
            {
               ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder(EmailValue.class);

               builder.prototype().from().set(administrator.contact().get().defaultEmail().emailAddress().get());
               builder.prototype().fromName().set(((Describable) self.owner().get()).getDescription());
               builder.prototype().subject().set(bundle.getString( "subject" ) + self.caseId().get()); 
               builder.prototype().content().set(bundle.getString( "message" ));
               builder.prototype().contentType().set("text/plain");
               builder.prototype().to().set(email.emailAddress().get());

               try
               {
                  // Store case as PDF for attachment purposes
                  ValueBuilder<CaseOutputConfigDTO> config = module.valueBuilderFactory().newValueBuilder(CaseOutputConfigDTO.class);
                  config.prototype().attachments().set(true);
                  config.prototype().contacts().set(true);
                  config.prototype().conversations().set(true);
                  config.prototype().submittedForms().set(true);
                  config.prototype().history().set(true);
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
   }
}