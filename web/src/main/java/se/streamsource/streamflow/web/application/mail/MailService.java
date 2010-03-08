/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.application.mail;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.Contactable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.json.JSONObject;
import se.streamsource.streamflow.web.domain.interaction.gtd.CompletableId;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationOwner;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.task.Task;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.Security;
import java.text.MessageFormat;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Send mail. This service
 * listens for domain events, and on "receivedMessage" it will send
 * a mail to the provided recepient.
 */
@Mixins(MailService.Mixin.class)
public interface MailService
      extends Configuration, Activatable, ServiceComposite
{
   public void sendNotification( DomainEvent event ) throws Exception;

   abstract class Mixin
         implements Activatable, MailService
   {
      @Structure
      UnitOfWorkFactory uowf;

      @This
      Configuration<MailConfiguration> config;

      public Logger logger;

      Properties props;

      public void activate() throws Exception
      {
         logger = Logger.getLogger( MailService.class.getName() );
         logger.info( "Initializing ..." );

         // Setup mail server
         props = new Properties();

         props.put( "mail.smtp.host", config.configuration().smtphost() );
         props.put( "mail.transport.protocol", "smtp" );
         props.put( "mail.debug", config.configuration().debug().get() );

         if (config.configuration().useSSL().get())
         {
            Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
            props.put( "mail.smtp.host", config.configuration().smtphost().get() );
            props.put( "mail.smtp.auth", "true" );
            props.put( "mail.smtp.port", config.configuration().smtpport().get() );
            props.put( "mail.smtp.socketFactory.port", config.configuration().smtpport().get() );
            props.put( "mail.smtp.socketFactory.class",
                  "javax.net.ssl.SSLSocketFactory" );
            props.put( "mail.smtp.socketFactory.fallback", "false" );
            props.setProperty( "mail.smtp.quitwait", "false" );

         } else if (config.configuration().useTLS().get())
         {
            props.put( "mail.smtp.startTLS", "true" );
            props.put( "mail.smtp.auth", "true" );
            props.put( "mail.smtp.port", config.configuration().smtpport().get() );
         }


         for (Map.Entry entry : props.entrySet())
         {
            logger.info( entry.getKey() + "=" + entry.getValue() );
         }
         logger.info( "Done" );
      }

      public void passivate() throws Exception
      {

      }

      public void sendNotification( DomainEvent event ) throws Exception
      {
         Contactable user = uowf.currentUnitOfWork().get( Contactable.class, event.entity().get() );
         ListIterator<ContactEmailValue> listIter = user.getContact().emailAddresses().get().listIterator();
         String emailAddress = "";
         if (listIter.hasNext())
         {
            emailAddress = listIter.next().emailAddress().get();
         }

         String messageId = new JSONObject( event.parameters().get() ).getString( "param1" );

         Message.Data message = uowf.currentUnitOfWork().get(Message.Data.class, messageId);
         Conversation conversation = message.conversation().get();
         ConversationOwner owner = conversation.conversationOwner().get();

         String taskId =  ((CompletableId.Data)owner).taskId().get() != null ? ((CompletableId.Data)owner).taskId().get() : "n/a" ;

         Object[] msgArgs = {taskId, ((Task)owner).getDescription(), conversation.getDescription()};
         String msgPattern = "There is a new message for \r\n TaskId: {0} \r\n Description: {1} \r\n Topic: {2}";

         String subject = "Notification: New Message available";

         if (emailAddress != null && emailAddress.length() != 0)
         {


            Session session = Session.getDefaultInstance( props,
                  new javax.mail.Authenticator()
                  {
                     protected PasswordAuthentication getPasswordAuthentication()
                     {
                        return new PasswordAuthentication( config.configuration().user().get(),
                              config.configuration().password().get() );
                     }
                  } );

            session.setDebug( config.configuration().debug().get() );

            MimeMessage msg = new MimeMessage( session );
            msg.setSender( new InternetAddress( config.configuration().from().get() ) );
            msg.setSubject( subject );
            msg.setContent( MessageFormat.format( msgPattern, msgArgs ), "text/plain" );

            msg.setRecipient( javax.mail.Message.RecipientType.TO, new InternetAddress( emailAddress ) );


            Transport.send( msg );


            logger.info( "Sent mail to " + emailAddress );
         }
      }
   }
}