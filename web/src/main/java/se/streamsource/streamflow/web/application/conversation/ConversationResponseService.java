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
import org.qi4j.api.specification.Specification;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Iterables;
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
import se.streamsource.streamflow.web.domain.structure.caze.History;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.created.Creator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

               if (references != null)
               {
                  // This is a response - handle it!

                  List<String> refs = (List<String>) Iterables.addAll((Collection<String>) new ArrayList<String>(), Iterables.iterable(references.split("[ \r\n\t]")));
                  Collections.reverse(refs);
                  String lastRef = Iterables.first(Iterables.filter(new Specification<String>()
                  {
                     public boolean satisfiedBy(String item)
                     {
                        return item.endsWith("@Streamflow>");
                     }
                  }, refs));

                  Matcher matcher = Pattern.compile("<([^/]*)/([^@]*)@[^>]*>").matcher(lastRef);

                  if (matcher.find())
                  {
                     String conversationId = matcher.group(1);
                     String participantId = matcher.group(2);

                     if (!"".equals( conversationId ) && !"".equals( participantId ))
                     {
                        ConversationParticipant from = uow.get( ConversationParticipant.class, participantId );
                        Conversation conversation = uow.get( Conversation.class, conversationId );

                        History history = (History) conversation.conversationOwner().get();
                        if (history.getHistory().equals(conversation))
                        {
                           // Response to history notification
                           // Find conversation for this user
                           Conversations.Data conversationsData = (Conversations.Data) history;
                           for (Conversation conversation1 : conversationsData.conversations())
                           {
                              if (conversation1.isParticipant(from))
                                 conversation = conversation1;
                           }

                           if (conversation.equals(history.getHistory()))
                           {
                              // Could not find a good conversation to put this message in - so create one
                              Conversations conversations = (Conversations) history;
                              conversation = conversations.createConversation(email.subject().get(), (Creator) from);
                           }
                        }

                        String content = email.content().get();

                        conversation.createMessage( content, from );
                     }
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