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
package se.streamsource.streamflow.web.rest.service.filter;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.TransactionTrackerMixin;
import se.streamsource.streamflow.web.application.mail.MailSender;
import se.streamsource.streamflow.web.context.services.ApplyFilterContext;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

/**
 * Applies filters on Inboxes. Listens for DomainEvents and when filter rules pass, then actions are applied
 */
@Mixins({FilterService.FilterMixin.class, TransactionTrackerMixin.class})
public interface FilterService
   extends Configuration, Ownable.Events, Assignable.Events, Status.Events, MailSender, Activatable, ServiceComposite
{
   class FilterMixin
      implements Ownable.Events, Assignable.Events, Status.Events
   {
      protected ApplyFilterContext applyFilterContext;

      public FilterMixin(@Structure Module module, @This MailSender mailSender, @Service AttachmentStore attachmentStore)
      {
         applyFilterContext = new ApplyFilterContext(module, mailSender, attachmentStore);
      }

      @This
      Configuration<FilterConfiguration> config;

      @Structure
      Module module;

      public void changedOwner(@Optional DomainEvent event, Owner newOwner)
      {
         if (event.entityType().get().equals(CaseEntity.class.getName()))
         {
            if (newOwner instanceof Filters.Data)
            {
               CaseEntity caze = module.unitOfWorkFactory().currentUnitOfWork().get(CaseEntity.class, event.entity().get());

               if (!caze.isAssigned() && caze.isStatus(CaseStates.OPEN) && lastNotificationInSeconds( caze ) > 10 )
               {
                  applyFilterContext.rebind((Filters.Data) newOwner, caze).applyFilters();
                  caze.updateNotificationTrace( new DateTime( ), "changedOwner" );
               }
            }
         }
      }

      public void assignedTo(@Optional DomainEvent event, Assignee assignee)
      {
         // Ignore
      }

      public void unassigned(@Optional DomainEvent event)
      {
         if (event.entityType().get().equals(CaseEntity.class.getName()))
         {
            CaseEntity caze = module.unitOfWorkFactory().currentUnitOfWork().get(CaseEntity.class, event.entity().get());
            Owner owner = caze.owner().get();
            if (owner instanceof Filters.Data)
            {
               if (caze.isStatus(CaseStates.OPEN) && lastNotificationInSeconds( caze ) > 10)
               {
                  applyFilterContext.rebind((Filters.Data) owner, caze).applyFilters();
                  caze.updateNotificationTrace( new DateTime(  ), "unassigned" );
               }
            }
         }
      }

      public void changedStatus(@Optional DomainEvent event, CaseStates status)
      {
         // omit use case create case from email to avoid sending twice - changeOwner has already triggered a send mail.
          if (event.entityType().get().equals(CaseEntity.class.getName())
               && status.equals(CaseStates.OPEN)
               && !( event.usecase().get().equals("Create case from email") ||
                event.usecase().get().equals("submitandsend") ))
         {
            CaseEntity caze = module.unitOfWorkFactory().currentUnitOfWork().get(CaseEntity.class, event.entity().get());
            Owner owner = caze.owner().get();
            if (owner instanceof Filters.Data && lastNotificationInSeconds( caze ) > 10)
            {
               applyFilterContext.rebind((Filters.Data) owner, caze).applyFilters();
               caze.updateNotificationTrace( new DateTime( ), "changedStatus" );
            }
         }
      }

      private int lastNotificationInSeconds( Case caze )
      {
         DateTime now = new DateTime(  );
         return new Duration( caze.getNotifiedOn() != null ? caze.getNotifiedOn() : now.minusSeconds( 60 ) , now ).toStandardSeconds().getSeconds();
      }
   }
}
