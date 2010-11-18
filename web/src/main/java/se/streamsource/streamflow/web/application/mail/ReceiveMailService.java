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

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Receive mail. This service
 * listens for domain events, and on "receivedMessage" it will send
 * a mail to the provided recipient. Furthermore a mail receiver is started that
 * polls the mail inbox for new replies which can be added to conversations.
 */
@Mixins(ReceiveMailService.Mixin.class)
public interface ReceiveMailService
      extends Configuration, Activatable, ServiceComposite
{
   abstract class Mixin
         implements Activatable, ReceiveMailService, Runnable
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @This
      Configuration<ReceiveMailConfiguration> config;

      @This
      MailReceiver mailReceiver;

      public Logger logger;

      private ScheduledExecutorService receiverExecutor;

      Authenticator authenticator;
      private Properties props;
      private URLName url;

      public void activate() throws Exception
      {
         logger = LoggerFactory.getLogger( ReceiveMailService.class );
         logger.info( "Initializing ..." );

         if (config.configuration().enabled().get())
         {
            // Authenticator
            authenticator = new javax.mail.Authenticator()
            {
               protected PasswordAuthentication getPasswordAuthentication()
               {
                  return new PasswordAuthentication( config.configuration().user().get(),
                        config.configuration().password().get() );
               }
            };

            props = new Properties();

            String protocol = config.configuration().protocol().get();
            props.put( "mail." + protocol + ".host", config.configuration().host().get() );
            props.put( "mail.transport.protocol", protocol );
            props.put( "mail." + protocol + ".auth", "true" );
            props.put( "mail." + protocol + ".port", config.configuration().port().get() );

            if (config.configuration().useSSL().get())
            {

               props.setProperty( "mail." + protocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory" );
               props.setProperty( "mail." + protocol + ".socketFactory.fallback", "false" );
               props.setProperty( "mail." + protocol + ".socketFactory.port", "" + config.configuration().port().get() );
            }

            url = new URLName( protocol, config.configuration().host().get(), config.configuration().port().get(), "",
                  config.configuration().user().get(), config.configuration().password().get() );

            long sleep = config.configuration().sleepPeriod().get();
            logger.info( "Starting scheduled mail receiver thread. Checking every: "
                  + (sleep == 0 ? 10 : sleep) + " min" );
            receiverExecutor = Executors.newSingleThreadScheduledExecutor();
            receiverExecutor.scheduleAtFixedRate( this, sleep, (sleep == 0 ? 10 : sleep), TimeUnit.MINUTES );

            logger.info( "Done" );
         }
      }

      public void passivate() throws Exception
      {
         receiverExecutor.shutdown();
         receiverExecutor.awaitTermination( 30, TimeUnit.SECONDS );
         logger.info( "Mail service shutdown" );
      }

      public void run()
      {
         logger.info( "Running mail receiver." );

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

               try
               {
                  ValueBuilder<EmailValue> builder = vbf.newValueBuilder( EmailValue.class );
                  builder.prototype().from().set( message.getFrom()[0].toString() );
                  builder.prototype().to().set( message.getRecipients( Message.RecipientType.TO ).toString() );
                  builder.prototype().subject().set( message.getSubject() );

                  Object content = message.getContent();
                  String body = "";
                  if (content instanceof String)
                  {
                     body = content.toString();
                     builder.prototype().content().set( body );
                     builder.prototype().contentType().set( "text/plain" );
                  } else if (content instanceof Multipart)
                  {
                     BodyPart part = ((Multipart) content).getBodyPart( 0 );
                     body = (String) part.getContent();
                     builder.prototype().content().set( body );
                     builder.prototype().contentType().set( part.getContentType() );
                  }

                  for (Header header : Iterables.iterable( (Enumeration<Header>) message.getAllHeaders() ))
                  {
                     builder.prototype().headers().get().put( header.getName(), header.getValue() );
                  }

                  mailReceiver.receivedEmail( null, builder.newInstance() );

                  uow.complete();
               } catch (Throwable e)
               {
                  uow.discard();
                  logger.error( "Could not parse emails", e );
               }
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
               logger.error( "Could not close inbox", e1 );
            }
         }
      }
   }
}