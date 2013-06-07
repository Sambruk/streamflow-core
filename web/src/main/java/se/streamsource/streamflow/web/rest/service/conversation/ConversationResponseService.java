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
package se.streamsource.streamflow.web.rest.service.conversation;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.io.Output;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;
import se.streamsource.streamflow.infrastructure.event.application.TransactionApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventPlayer;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventReplayException;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventSource;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventStream;
import se.streamsource.streamflow.infrastructure.event.application.source.helper.ApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.source.helper.ApplicationTransactionTracker;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.MailReceiver;
import se.streamsource.streamflow.web.context.workspace.cases.CaseCommandsContext;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLoggable;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;

import java.net.URLDecoder;
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
      
      @Service
      SystemDefaultsService systemDefaults;

      @Structure
      Module module;

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
            extends MailReceiver.Mixin
      {

         public void receivedEmail( ApplicationEvent event, EmailValue email )
         {
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(UsecaseBuilder.newUsecase("Receive email in conversation"));

            try
            {
               String references = email.headers().get().get( "References" );

               if ( hasStreamflowReference( references ) )
               {
                  // This is a response - handle it!

                  List<String> refs = (List<String>) Iterables.addAll((Collection<String>) new ArrayList<String>(), Iterables.iterable(references.split("[ \r\n\t]")));
                  
                  // Hotmail handles refs a bit differently...
                  String hotmailRefs = Iterables.first( Iterables.filter(new Specification<String>()
                  {
                     public boolean satisfiedBy(String item)
                     {
                        return item.contains( "," ) && item.endsWith("@Streamflow>");
                     }
                  }, refs));
                  
                  String lastRef = null;
                  if (!Strings.empty( hotmailRefs ))
                  {
                     lastRef = hotmailRefs.split( "," )[1];
                  } else
                  {
                     Collections.reverse( refs );
                     Iterable<String> filter = Iterables.filter( new Specification<String>()
                     {
                        public boolean satisfiedBy(String item)
                        {
                           return item.endsWith( "@Streamflow>" );
                        }
                     }, refs );
                     lastRef = Iterables.first( filter );
                  }
                 
                  
                  if (lastRef == null)
                  {
                     ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder( EmailValue.class ).withPrototype( email );
                     String subj = "Msg Ref missing: " + builder.prototype().subject().get();
                     builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );

                     systemDefaults.createCaseOnEmailFailure( builder.newInstance() );
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
                        String content = email.content().get();

                        // If we have an assignee, ensure it is a member of the conversation first
                        if (caze.isAssigned())
                        {
                           if (!conversation.isParticipant((ConversationParticipant) caze.assignedTo().get()))
                              conversation.addParticipant((ConversationParticipant) caze.assignedTo().get());
                        }

                        // Create a new role map and fill it with relevant objects
                        if( RoleMap.current() == null )
                           RoleMap.newCurrentRoleMap();
                        RoleMap.current().set( from, ConversationParticipant.class );
                        RoleMap.current().set( caze, CaseLoggable.Data.class );

                        Message message = null;

                        if( Translator.HTML.equalsIgnoreCase( email.contentType().get() ))
                        {
                           message = conversation.createMessage( email.contentHtml().get(), MessageType.HTML, from );
                        } else
                        {
                           message = conversation.createMessage( email.content().get(), MessageType.PLAIN, from );
                        }
                        // Create attachments
                        for (AttachedFileValue attachedFileValue : email.attachments().get())
                        {
                           if (! (attachedFileValue.mimeType().get().contains("text/x-vcard")
                                 || attachedFileValue.mimeType().get().contains("text/directory")) )
                           {
                              Attachment attachment = message.createAttachment(attachedFileValue.uri().get());
                              attachment.changeDescription( "New Attachment" );
                              attachment.changeName(attachedFileValue.name().get());
                              attachment.changeMimeType(attachedFileValue.mimeType().get());
                              attachment.changeModificationDate(attachedFileValue.modificationDate().get());
                              attachment.changeSize(attachedFileValue.size().get());
                              attachment.changeUri(attachedFileValue.uri().get());
                           }
                        }

                        try
                        {
                           if( caze.isStatus( CaseStates.CLOSED ))
                           {
                              RoleMap.newCurrentRoleMap();
                              RoleMap.current().set( caze );
                              if( caze.assignedTo().get() != null )
                              {
                                RoleMap.current().set( caze.assignedTo().get() );
                              } else
                              {
                                 RoleMap.current().set( uow.get( UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME ) );
                              }
                              CaseCommandsContext caseCommands = module.transientBuilderFactory().newTransient( CaseCommandsContext.class );
                              caseCommands.reopen();
                              caseCommands.unassign();
                              RoleMap.clearCurrentRoleMap();
                           }
                        } catch(Throwable e )
                        {
                           ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder( EmailValue.class ).withPrototype( email );
                           String subj = "Create Case failed: " + builder.prototype().subject().get();
                           builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );

                           systemDefaults.createCaseOnEmailFailure( builder.newInstance() );
                           throw new IllegalStateException("Could not open case through new message.", e);
                        }
                     }
                  }
               }

               uow.complete();
            } catch (Exception ex)
            {
               ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder( EmailValue.class ).withPrototype( email );
               String subj = "Conversation Response Error: " + builder.prototype().subject().get();
               builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );

               systemDefaults.createCaseOnEmailFailure( builder.newInstance() );
               uow.discard();
               throw new ApplicationEventReplayException(event, ex);
            }
         }
      }
   }
}