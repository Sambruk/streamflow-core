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

package se.streamsource.streamflow.web.application.conversation;

import org.qi4j.api.configuration.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.io.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.service.*;
import org.qi4j.api.specification.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.usecase.*;
import org.qi4j.api.util.*;
import org.slf4j.*;
import se.streamsource.streamflow.infrastructure.event.application.*;
import se.streamsource.streamflow.infrastructure.event.application.replay.*;
import se.streamsource.streamflow.infrastructure.event.application.source.*;
import se.streamsource.streamflow.infrastructure.event.application.source.helper.*;
import se.streamsource.streamflow.web.application.mail.*;
import se.streamsource.streamflow.web.domain.entity.caze.*;
import se.streamsource.streamflow.web.domain.structure.conversation.*;
import se.streamsource.streamflow.web.domain.structure.created.*;

import java.net.*;
import java.util.*;
import java.util.regex.*;

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

      private Logger logger = LoggerFactory.getLogger(ConversationResponseService.class);

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
                  
                  if (lastRef == null)
                  {
                     logger.error("Could not find message reference in email header:"+lastRef);
                     uow.discard();
                     return;
                  }

                  Matcher matcher = Pattern.compile("<([^/]*)/([^@]*)@[^>]*>").matcher(lastRef);

                  if (matcher.find())
                  {
                     String conversationId = matcher.group(1);
                     String participantId = URLDecoder.decode(matcher.group(2), "UTF-8");

                     if (!"".equals( conversationId ) && !"".equals( participantId ))
                     {
                        ConversationParticipant from = uow.get( ConversationParticipant.class, participantId );
                        Conversation conversation = uow.get( Conversation.class, conversationId );

                        CaseEntity caze = (CaseEntity) conversation.conversationOwner().get();
                        if (caze.getHistory().equals(conversation))
                        {
                           // Response to history notification
                           // Find conversation for this user
                           Conversations.Data conversationsData = (Conversations.Data) caze;
                           for (Conversation conversation1 : conversationsData.conversations())
                           {
                              if (conversation1.isParticipant(from))
                                 conversation = conversation1;
                           }

                           if (conversation.equals(caze.getHistory()))
                           {
                              // Could not find a good conversation to put this message in - so create one
                              Conversations conversations = (Conversations) caze;
                              conversation = conversations.createConversation(email.subject().get(), (Creator) from);
                           }
                        }

                        String content = email.content().get();

                        // If we have an assignee, ensure it is a member of the conversation first
                        if (caze.isAssigned())
                        {
                           if (!conversation.isParticipant((ConversationParticipant) caze.assignedTo().get()))
                              conversation.addParticipant((ConversationParticipant) caze.assignedTo().get());
                        }

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