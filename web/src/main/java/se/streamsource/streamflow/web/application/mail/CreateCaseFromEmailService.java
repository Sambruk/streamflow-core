/*
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

package se.streamsource.streamflow.web.application.mail;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.io.Output;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import se.streamsource.dci.api.Contexts;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;
import se.streamsource.streamflow.infrastructure.event.application.TransactionApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventPlayer;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventReplayException;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventSource;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventStream;
import se.streamsource.streamflow.infrastructure.event.application.source.helper.ApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.source.helper.ApplicationTransactionTracker;
import se.streamsource.streamflow.web.application.conversation.ConversationResponseConfiguration;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsQueries;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.Contacts;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.created.Creator;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;

/**
 * Receive emails and create cases through Access Points
 */
@Mixins(CreateCaseFromEmailService.Mixin.class)
public interface CreateCaseFromEmailService
        extends Configuration, Activatable, ServiceComposite
{
   class Mixin
           implements Activatable
   {
      @Service
      ApplicationEventSource eventSource;

      @Service
      ApplicationEventStream stream;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      QueryBuilderFactory qbf;

      @This
      Configuration<CreateCaseFromEmailConfiguration> config;

      private ApplicationTransactionTracker<ApplicationEventReplayException> tracker;

      @Service
      ApplicationEventPlayer player;

      private ReceiveEmails receiveEmails = new ReceiveEmails();

      public void activate() throws Exception
      {
         Output<TransactionApplicationEvents, ApplicationEventReplayException> playerOutput = ApplicationEvents.playEvents(player, receiveEmails);

         tracker = new ApplicationTransactionTracker<ApplicationEventReplayException>(stream, eventSource, config, playerOutput);
         tracker.start();
      }

      public void passivate() throws Exception
      {
         tracker.stop();
      }

      public class ReceiveEmails
              implements MailReceiver
      {
         public void receivedEmail(ApplicationEvent event, EmailValue email)
         {
            UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Create case from email"));

            try
            {
               String references = email.headers().get().get("References");

               // This is not in response to something that we sent out - create new case from it
               if (references == null)
               {
                  OrganizationsQueries organizations = uow.get(OrganizationsQueries.class, OrganizationsEntity.ORGANIZATIONS_ID);
                  Organization organization = organizations.organizations().newQuery(uow).find();
                  AccessPoint ap = null;
                  try
                  {
                     ap = organization.getAccessPoint(email.to().get());
                  } catch (IllegalArgumentException e)
                  {
                     // No AP for this email address - ok!
                     uow.discard();
                     return;
                  }

                  Drafts user = getUser(email);

                  RoleMap.newCurrentRoleMap();
                  RoleMap.current().set(organization);
                  RoleMap.current().set(ap);
                  RoleMap.current().set(user);

                  Case caze = ap.createCase(user);

                  caze.changeDescription(email.subject().get());
                  caze.changeNote(email.content().get());

                  // Create conversation
                  Conversation conversation = caze.createConversation(email.subject().get(), (Creator) user);
                  conversation.createMessage(email.content().get(), (ConversationParticipant) user);

                  // Open the case
                  ap.sendTo(caze);
               }

               uow.complete();
            } catch (Exception ex)
            {
               uow.discard();
               throw new ApplicationEventReplayException(event, ex);
            } finally
            {
               RoleMap.clearCurrentRoleMap();
            }
         }

         private Drafts getUser(EmailValue email)
         {
            // Try to find real user first
            Query<Drafts> finduserwithemail = qbf.newNamedQuery(Drafts.class, uowf.currentUnitOfWork(), "finduserwithemail");
            finduserwithemail.setVariable("email", "[{\"contactType\":\"HOME\",\"emailAddress\":\"" + email.from().get() + "\"}]");
            Drafts user = finduserwithemail.find();

            return user;
         }

         private AccessPoint getAccessPoint(AccessPoints.Data organization)
         {
            AccessPoints.Data aps = organization;
            if (aps.accessPoints().count() > 0)
            {
               // TODO make this configurable
               return aps.accessPoints().get(0);
            }
            return null;
         }
      }
   }
}