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

package se.streamsource.streamflow.web.application.mail;

import org.qi4j.api.configuration.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.io.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.service.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.usecase.*;
import org.qi4j.api.util.*;
import org.qi4j.api.value.*;
import org.qi4j.spi.service.*;
import org.slf4j.*;
import se.streamsource.infrastructure.*;
import se.streamsource.infrastructure.circuitbreaker.*;
import se.streamsource.infrastructure.circuitbreaker.service.*;
import se.streamsource.streamflow.web.domain.structure.attachment.*;
import se.streamsource.streamflow.web.infrastructure.attachment.*;

import javax.mail.*;
import javax.mail.internet.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Receive mail. This service
 * listens for domain events, and on "receivedMessage" it will send
 * a mail to the provided recipient. Furthermore a mail receiver is started that
 * polls the mail inbox for new replies which can be added to conversations.
 */
@Mixins({ReceiveMailService.Mixin.class, AbstractEnabledCircuitBreakerAvailability.class})
public interface ReceiveMailService
        extends Configuration, Activatable, MailReceiver, ServiceComposite, ServiceCircuitBreaker, AbstractEnabledCircuitBreakerAvailability
{
   abstract class Mixin
           implements Activatable, ReceiveMailService, Runnable, ServiceCircuitBreaker, VetoableChangeListener
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @This
      Configuration<ReceiveMailConfiguration> config;

      @This
      MailReceiver mailReceiver;

      @Uses
      ServiceDescriptor descriptor;

      @org.qi4j.api.injection.scope.Service
      AttachmentStore attachmentStore;

      public Logger logger;

      private ScheduledExecutorService receiverExecutor;

      Authenticator authenticator;
      private Properties props;
      private URLName url;

      private CircuitBreaker circuitBreaker;

      public void activate() throws Exception
      {
         circuitBreaker = descriptor.metaInfo(CircuitBreaker.class);

         logger = LoggerFactory.getLogger(ReceiveMailService.class);

         if (config.configuration().enabled().get())
         {
            // Authenticator
            authenticator = new javax.mail.Authenticator()
            {
               protected PasswordAuthentication getPasswordAuthentication()
               {
                  return new PasswordAuthentication(config.configuration().user().get(),
                          config.configuration().password().get());
               }
            };

            props = new Properties();

            String protocol = config.configuration().protocol().get();
            props.put("mail." + protocol + ".host", config.configuration().host().get());
            props.put("mail.transport.protocol", protocol);
            props.put("mail." + protocol + ".auth", "true");
            props.put("mail." + protocol + ".port", config.configuration().port().get());

            if (config.configuration().useSSL().get())
            {

               props.setProperty("mail." + protocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
               props.setProperty("mail." + protocol + ".socketFactory.fallback", "false");
               props.setProperty("mail." + protocol + ".socketFactory.port", "" + config.configuration().port().get());
            }

            url = new URLName(protocol, config.configuration().host().get(), config.configuration().port().get(), "",
                    config.configuration().user().get(), config.configuration().password().get());


            circuitBreaker.addVetoableChangeListener(this);
            circuitBreaker.turnOn();

            long sleep = config.configuration().sleepPeriod().get();
            logger.info("Starting scheduled mail receiver thread. Checking every: "
                    + (sleep == 0 ? 10 : sleep) + " min");
            receiverExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ReceiveMail"));
            receiverExecutor.scheduleWithFixedDelay(this, 0, (sleep == 0 ? 10 : sleep), TimeUnit.MINUTES);
         }
      }

      public void passivate() throws Exception
      {
         circuitBreaker.removeVetoableChangeListener(this);

         if (receiverExecutor != null)
         {
            receiverExecutor.shutdown();
            receiverExecutor.awaitTermination(30, TimeUnit.SECONDS);
         }

         logger.info("Mail service shutdown");
      }

      public CircuitBreaker getCircuitBreaker()
      {
         return circuitBreaker;
      }

      public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException
      {
         // Test connection to mail server
         if (evt.getNewValue() == CircuitBreaker.Status.on)
         {
            Session session = javax.mail.Session.getInstance(props, authenticator);
            session.setDebug(config.configuration().debug().get());
            try
            {
               Store store = session.getStore(url);
               store.connect();
               store.close();
            } catch (MessagingException e)
            {
               // Failed - don't allow to turn on circuit breaker
               throw new PropertyVetoException(e.getMessage(), evt);
            }
         }
      }

      public void run()
      {
         Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

         if (!circuitBreaker.isOn())
            return; // Don't try - circuit breaker is off

         logger.info("Checking email");

         Session session = javax.mail.Session.getInstance(props, authenticator);
         session.setDebug(config.configuration().debug().get());

         Usecase usecase = UsecaseBuilder.newUsecase("Receive Mail");
         UnitOfWork uow = null;
         Store store = null;
         Folder inbox = null;

         try
         {
            store = session.getStore(url);
            store.connect();

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            javax.mail.Message[] messages = inbox.getMessages();
            FetchProfile fp = new FetchProfile();
//            fp.add( "In-Reply-To" );
            inbox.fetch(messages, fp);

            for (javax.mail.Message message : messages)
            {
               uow = uowf.newUnitOfWork(usecase);

               try
               {
                  Object content = message.getContent();

                  // Get body and attachments
                  ValueBuilder<EmailValue> builder = vbf.newValueBuilder(EmailValue.class);
                  String body = "";
                  if (content instanceof String)
                  {
                     body = content.toString();
                     builder.prototype().content().set(body);
                     builder.prototype().contentType().set(message.getContentType());
                  } else if (content instanceof Multipart)
                  {
                     Multipart multipart = (Multipart) content;
                     for (int i = 0, n = multipart.getCount(); i < n; i++)
                     {
                        BodyPart part = multipart.getBodyPart(i);

                        String disposition = part.getDisposition();

                        if ((disposition != null) &&
                                ((disposition.equals( Part.ATTACHMENT) ||
                                        (disposition.equals(Part.INLINE)))))
                        {
                           // Create attachment
                           ValueBuilder<AttachedFileValue> attachmentBuilder = vbf.newValueBuilder(AttachedFileValue.class);

                           AttachedFileValue prototype = attachmentBuilder.prototype();
                           prototype.mimeType().set(part.getContentType());
                           prototype.modificationDate().set((message.getSentDate()));
                           prototype.name().set(part.getFileName());
                           prototype.size().set((long) part.getSize());

                           InputStream inputStream = part.getInputStream();
                           String id = attachmentStore.storeAttachment(Inputs.byteBuffer(inputStream, 4096));
                           String uri = "store:"+id;
                           prototype.uri().set(uri);

                           builder.prototype().attachments().get().add(attachmentBuilder.newInstance());
                        } else
                        {
                           body = (String) part.getContent();
                           builder.prototype().content().set(body);
                           builder.prototype().contentType().set(part.getContentType());
                        }
                     }
                  } else if (content instanceof InputStream)
                  {
                     content = new MimeMessage(session, (InputStream) content).getContent();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     Inputs.byteBuffer((InputStream) content, 4096).transferTo(Outputs.byteBuffer(baos));

                     String data = new String(baos.toByteArray(), "UTF-8");
                     // Unknown content type - abort
                     uow.discard();
                     continue;
                  }else
                  {
                     // Unknown content type - abort
                     uow.discard();
                     logger.error("Could not parse emails: unknown content type "+content.getClass().getName());
                     continue;
                  }

                  // Get email fields
                  builder.prototype().from().set(((InternetAddress) message.getFrom()[0]).getAddress());
                  builder.prototype().fromName().set(((InternetAddress) message.getFrom()[0]).getPersonal());
                  builder.prototype().to().set(((InternetAddress) message.getRecipients(Message.RecipientType.TO)[0]).getAddress());
                  builder.prototype().subject().set(message.getSubject());

                  // Get headers
                  for (Header header : Iterables.iterable((Enumeration<Header>) message.getAllHeaders()))
                  {
                     builder.prototype().headers().get().put(header.getName(), header.getValue());
                  }

                  builder.prototype().messageId().set(message.getHeader("Message-ID")[0]);

                  mailReceiver.receivedEmail(null, builder.newInstance());

                  uow.complete();
               } catch (Throwable e)
               {
                  uow.discard();
                  logger.error("Could not parse emails", e);
               }
            }
            inbox.close(false);
            store.close();

            logger.info("Checked email");

            circuitBreaker.success();
         } catch (Throwable e)
         {
            circuitBreaker.throwable(e);

            try
            {
               if (inbox != null && inbox.isOpen())
                  inbox.close(false);

               if (store != null && store.isConnected())
                  store.close();
            } catch (Throwable e1)
            {
               logger.error("Could not close inbox", e1);
            }
         }
      }
   }
}