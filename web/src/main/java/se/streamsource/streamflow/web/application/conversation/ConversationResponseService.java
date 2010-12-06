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

package se.streamsource.streamflow.web.application.conversation;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.io.Output;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;
import se.streamsource.streamflow.infrastructure.event.application.TransactionApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventPlayer;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventReplayException;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventSource;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventStream;
import se.streamsource.streamflow.infrastructure.event.application.source.helper.ApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.source.helper.ApplicationTransactionTracker;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.MailReceiver;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;

/**
 * Receive emails and create responses in conversations
 */
@Mixins(ConversationResponseService.Mixin.class)
public interface ConversationResponseService
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

      @This
      Configuration<ConversationResponseConfiguration> config;

      private ApplicationTransactionTracker<ApplicationEventReplayException> tracker;

      @Service
      ApplicationEventPlayer player;

      private ReceiveEmails receiveEmails = new ReceiveEmails();

      public void activate() throws Exception
      {
         Output<TransactionApplicationEvents, ApplicationEventReplayException> playerOutput = ApplicationEvents.playEvents( player, receiveEmails );

         tracker = new ApplicationTransactionTracker<ApplicationEventReplayException>( stream, eventSource, config, playerOutput );
         tracker.start();
      }

      public void passivate() throws Exception
      {
         tracker.stop();
      }

      public class ReceiveEmails
            implements MailReceiver
      {
         public void receivedEmail( ApplicationEvent event, EmailValue email )
         {
            UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "Receive email in conversation" ) );

            try
            {
               String references = email.headers().get().get( "References" );
               String lastRef = references.substring( references.lastIndexOf(' ' )+2); // Remove " <" from last message-id

               String[] ids = lastRef.split( "/" );
               if (ids.length == 2)
               {
                  String conversationId = ids[0];
                  String participantId = ids[1].split("@")[0];

                  if (!"".equals( conversationId ) && !"".equals( participantId ))
                  {
                     ConversationParticipant from = uow.get( ConversationParticipant.class, participantId );
                     Conversation conversation = uow.get( Conversation.class, conversationId );

                     String content = email.content().get();

                     conversation.createMessage( content, from );
                  }
               }

               uow.complete();
            } catch (Exception ex)
            {
               uow.discard();
               throw new ApplicationEventReplayException(event, ex);
            }
         }
      }
   }
}