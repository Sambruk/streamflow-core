/**
 *
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

import org.json.JSONObject;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.Contactable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.web.domain.interaction.gtd.CompletableId;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationOwner;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Send and receive mail. This service
 * listens for domain events, and on "receivedMessage" it will send
 * a mail to the provided recipient. Furthermore a mail receiver is started that
 * polls the mail inbox for new replies which can be added to conversations.
 */
@Mixins(MailService.Mixin.class)
public interface MailService
      extends Configuration, Activatable, ServiceComposite
{
   public void sendNotification( DomainEvent event ) throws Exception;

   abstract class Mixin
         implements Activatable, MailService, Runnable
   {
      @Structure
      UnitOfWorkFactory uowf;

      @This
      Configuration<MailConfiguration> config;

      public Logger logger;

      private ScheduledExecutorService mailReceiver;

      Properties props;
      Authenticator authenticator;

      public void activate() throws Exception
      {
         logger = LoggerFactory.getLogger( MailService.class );
         logger.info( "Initializing ..." );

         // Authenticator
         authenticator = new javax.mail.Authenticator()
         {
            protected PasswordAuthentication getPasswordAuthentication()
            {
               return new PasswordAuthentication( config.configuration().user().get(),
                     config.configuration().password().get() );
            }
         };

         // Setup mail server
         props = new Properties();

         props.put( "mail.smtp.host", config.configuration().smtphost() );
         props.put( "mail.transport.protocol", "smtp" );
         props.put( "mail.debug", config.configuration().debug().get() );

         if (config.configuration().useSSL().get())
         {
//            Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
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

         long delay = config.configuration().receiverDelay().get();
         long sleep = config.configuration().receiverSleepPeriod().get();
         logger.info( "Starting scheduled mail receiver thread. Delay: "
               + delay
               + " min  SleepPeriod: "
               + (sleep == 0 ? 10 : sleep) + " min" );
 //        mailReceiver = Executors.newSingleThreadScheduledExecutor();
//         mailReceiver.scheduleAtFixedRate( this, delay, (sleep == 0 ? 10 : sleep), TimeUnit.MINUTES );

         logger.info( "Done" );
      }

      public void passivate() throws Exception
      {
//         mailReceiver.shutdown();
//           mailReceiver.awaitTermination( 30, TimeUnit.SECONDS );
           logger.info( "Mail service shutdown" );
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

         String messageId = EventParameters.getParameter( event, 1);

         Message.Data message = uowf.currentUnitOfWork().get( Message.Data.class, messageId );
         Conversation conversation = message.conversation().get();
         ConversationOwner owner = conversation.conversationOwner().get();

         String sender = ((Contactable.Data)message.sender().get()).contact().get().name().get();
         String caseId = "n/a";

         if (owner != null)
            caseId = ((CompletableId.Data) owner).caseId().get() != null ? ((CompletableId.Data) owner).caseId().get() : "n/a";

         if (emailAddress != null && emailAddress.length() != 0)
         {


            Session session = Session.getInstance( props, authenticator );

            session.setDebug( config.configuration().debug().get() );

            MimeMessage msg = new MimeMessage( session );
            msg.setSender( new InternetAddress( config.configuration().from().get() ) );
            msg.setSubject( "[" + caseId + "]" + conversation.getDescription()
                  + "(" + EntityReference.getEntityReference( message.conversation().get() ).identity() + ":" + event.entity().get() + ")" );

            String formattedMsg = message.body().get();
            if (formattedMsg.contains( "<body>" ))
            {
               formattedMsg = formattedMsg.replace( "<body>", "<body><b>" + sender + ":</b><br/><br/>" );
            } else
            {
               formattedMsg = sender + ":\r\n\r\n" + formattedMsg;
            }

            msg.setContent( formattedMsg, "text/html" );

            msg.setRecipient( javax.mail.Message.RecipientType.TO, new InternetAddress( emailAddress ) );


            Transport.send( msg );


            logger.info( "Sent mail to " + emailAddress );
         }
      }

      public void run()
      {
         if (config.configuration().receiverEnabled().get())
         {
            logger.info( "Running mail receiver." );
            java.util.Properties props = new java.util.Properties();

            String protocol = config.configuration().receiverProtocol().get();
            props.put( "mail." + protocol + ".host", config.configuration().receiverHost().get() );
            props.put( "mail.transport.protocol", protocol );
            props.put( "mail." + protocol + ".auth", "true" );
            props.put( "mail." + protocol + ".port", config.configuration().receiverPort().get() );

            if (config.configuration().useSSL().get())
            {

               props.setProperty( "mail." + protocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory" );
               props.setProperty( "mail." + protocol + ".socketFactory.fallback", "false" );
               props.setProperty( "mail." + protocol + ".socketFactory.port", "" + config.configuration().receiverPort().get().intValue() );
            }

            if (config.configuration().debug().get())
            {
               for (Map.Entry prop : props.entrySet())
               {
                  logger.info( prop.getKey() + "=" + prop.getValue() );
               }
            }

            Session session = javax.mail.Session.getInstance( props, authenticator );
            session.setDebug( config.configuration().debug().get() );

            Usecase usecase = UsecaseBuilder.newUsecase( "Receive Mail" );
            UnitOfWork uow = null;
            Store store = null;
            Folder inbox = null;

            try
            {
               URLName url = new URLName( protocol, config.configuration().receiverHost().get(), config.configuration().receiverPort().get(), "",
                     config.configuration().user().get(), config.configuration().password().get() );

               store = session.getStore( url );
               store.connect();

               inbox = store.getFolder( "INBOX" );
               inbox.open( Folder.READ_ONLY );

               javax.mail.Message[] messages = inbox.getMessages();
               FetchProfile fp = new FetchProfile();
               fp.add( "In-Reply-To" );
               inbox.fetch( messages, fp );

               for (javax.mail.Message message : messages)
               {
                  uow = uowf.newUnitOfWork( usecase );

                  String[] parentMessageIds = message.getHeader( "In-Reply-To" );

                  if (parentMessageIds.length > 0)
                  {

                     String subject = message.getSubject();

                     String idString = subject.substring( subject.indexOf( '(' ) + 1, subject.indexOf( ')' ) );
                     String[] ids = idString.split( ":" );


                     String conversationId = ids[0];
                     String participantId = ids[1];

                     if (!"".equals( conversationId ) && !"".equals( participantId ))
                     {
                        Object obj = message.getContent();
                        String reply = "";
                        if (obj instanceof String)
                        {
                           reply = obj.toString();
                        } else if (obj instanceof Multipart)
                        {

                           BodyPart part = ((Multipart) obj).getBodyPart( 0 );
                           reply = (String) part.getContent();
                        }

                        Conversation conversation = uow.get( Conversation.class, conversationId );
                        conversation.createMessage( reply, uow.get( ConversationParticipant.class, participantId ) );

                     }

                  }
                  uow.complete();
               }
               inbox.close( false );
               store.close();

            } catch (Exception e)
            {
               try
               {
                  inbox.close( false );
                  store.close();
               } catch (MessagingException e1)
               {
                  e1.printStackTrace();
               }

               uow.discard();
            }
         }
      }
   }
}