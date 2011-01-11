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
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.service.ServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;
import se.streamsource.infrastructure.circuitbreaker.service.ServiceCircuitBreaker;
import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventPlayer;
import se.streamsource.streamflow.infrastructure.event.application.replay.ApplicationEventReplayException;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventSource;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventStream;
import se.streamsource.streamflow.infrastructure.event.application.source.helper.ApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.source.helper.ApplicationTransactionTracker;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

import static se.streamsource.infrastructure.circuitbreaker.CircuitBreakers.withBreaker;

/**
 * Send emails. This service
 * listens for application events, and on "sentEmail" it will send
 * the provided EmailValue.
 */
@Mixins(SendMailService.Mixin.class)
public interface SendMailService
      extends Configuration, ServiceCircuitBreaker, Activatable, ServiceComposite
{
   abstract class Mixin
         implements ServiceCircuitBreaker, Activatable
   {
      @org.qi4j.api.injection.scope.Service
      ApplicationEventSource source;

      @org.qi4j.api.injection.scope.Service
      ApplicationEventStream stream;

      @org.qi4j.api.injection.scope.Service
      ApplicationEventPlayer player;

      @This
      Configuration<SendMailConfiguration> config;

      @Uses
      ServiceDescriptor descriptor;

      public Logger logger;

      Properties props;
      Authenticator authenticator;

      ApplicationTransactionTracker<ApplicationEventReplayException> tracker;
      private CircuitBreaker circuitBreaker;

      public void activate() throws Exception
      {
         logger = LoggerFactory.getLogger( SendMailService.class );

         circuitBreaker = descriptor.metaInfo( CircuitBreaker.class );
         tracker = new ApplicationTransactionTracker<ApplicationEventReplayException>( stream,
               source,
               config,
               withBreaker( circuitBreaker,
                     ApplicationEvents.playEvents( player, new SendMails() ) ));

         if (config.configuration().enabled().get())
         {

            // Authenticator
            authenticator = new Authenticator()
            {
               protected PasswordAuthentication getPasswordAuthentication()
               {
                  return new PasswordAuthentication( config.configuration().user().get(),
                        config.configuration().password().get() );
               }
            };

            // Setup mail server
            props = new Properties();

            props.put( "mail.smtp.host", config.configuration().host() );
            props.put( "mail.transport.protocol", "smtp" );
            props.put( "mail.debug", config.configuration().debug().get() );

            if (config.configuration().useSSL().get())
            {
//            Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
               props.put( "mail.smtp.host", config.configuration().host().get() );
               props.put( "mail.smtp.auth", "true" );
               props.put( "mail.smtp.port", config.configuration().port().get() );
               props.put( "mail.smtp.socketFactory.port", config.configuration().port().get() );
               props.put( "mail.smtp.socketFactory.class",
                     "javax.net.ssl.SSLSocketFactory" );
               props.put( "mail.smtp.socketFactory.fallback", "false" );
               props.setProperty( "mail.smtp.quitwait", "false" );

            } else if (config.configuration().useTLS().get())
            {
               props.put( "mail.smtp.startTLS", "true" );
               props.put( "mail.smtp.auth", "true" );
               props.put( "mail.smtp.port", config.configuration().port().get() );
            }

            tracker.start();
         }
      }

      public void passivate() throws Exception
      {
         tracker.stop();
      }

      public CircuitBreaker getCircuitBreaker()
      {
         return circuitBreaker;
      }

      public class SendMails
            implements MailSender
      {
         public void sentEmail( ApplicationEvent event, EmailValue email )
         {
            try
            {
               Session session = Session.getInstance( props, authenticator );

               session.setDebug( config.configuration().debug().get() );

               SendMimeMessage msg = new SendMimeMessage( session, email );

               if (email.fromName().get() == null)
                  msg.setFrom( new InternetAddress( config.configuration().from().get() ) );
               else
                  msg.setFrom( new InternetAddress( config.configuration().from().get(), email.fromName().get(), "ISO-8859-1" ) );

               msg.setRecipient( javax.mail.Message.RecipientType.TO, new InternetAddress( email.to().get() ) );
               msg.setSubject( email.subject().get() );
               if (email.contentType().get().equals("text/plain"))
                  msg.setText( email.content().get(), "UTF-8" );
               else
                  msg.setContent( email.content().get(), email.contentType().get() );
               for (Map.Entry<String, String> header : email.headers().get().entrySet())
               {
                  msg.setHeader( header.getKey(), header.getValue() );
               }

               Transport.send( msg );

               logger.debug( "Sent mail to " + email.to().get() );
            } catch (Throwable e)
            {
               throw new ApplicationEventReplayException( event, e );
            }
         }
      }

      public static class SendMimeMessage
         extends MimeMessage
      {
         private final EmailValue email;

         public SendMimeMessage( Session session, EmailValue email )
         {
            super( session );
            this.email = email;
         }

         @Override
         protected void updateMessageID() throws MessagingException
         {
            String messageId = email.messageId().get();
            if (messageId != null)
               setHeader( "Message-ID", messageId );
            else
               super.updateMessageID();
         }

         @Override
         protected void updateHeaders() throws MessagingException
         {
            super.updateHeaders();
         }
      }
   }
}