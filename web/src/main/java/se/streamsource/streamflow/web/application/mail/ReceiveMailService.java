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

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.spi.service.ServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.infrastructure.NamedThreadFactory;
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;
import se.streamsource.infrastructure.circuitbreaker.service.AbstractEnabledCircuitBreakerAvailability;
import se.streamsource.infrastructure.circuitbreaker.service.ServiceCircuitBreaker;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.application.security.UserPrincipal;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.qi4j.api.usecase.UsecaseBuilder.*;

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
      Module module;

      @This
      Configuration<ReceiveMailConfiguration> config;

      @This
      MailReceiver mailReceiver;

      @Uses
      ServiceDescriptor descriptor;

      @Service
      AttachmentStore attachmentStore;

      @Service
      SystemDefaultsService systemDefaults;

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
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( newUsecase( "Create Streamflow support structure" ) );
            RoleMap.newCurrentRoleMap();
            RoleMap.current().set( uow.get( UserAuthentication.class, UserEntity.ADMINISTRATOR_USERNAME ) );
            RoleMap.current().set( new UserPrincipal( UserEntity.ADMINISTRATOR_USERNAME ) );
            
            
            Organizations.Data orgs = uow.get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
            OrganizationEntity org = (OrganizationEntity)orgs.organization().get();
            // check for the existance of support structure for mails that cannot be parsed
            RoleMap.current().set( org.getAdministratorRole() );
            
            OrganizationalUnit ou = null;
            Project project = null;
            CaseType caseType = null;

            try
            {
               try
               {
                  ou = org.getOrganizationalUnitByName( systemDefaults.config().configuration().supportOrganizationName().get() );
               } catch (IllegalArgumentException iae)
               {
                  ou = org.createOrganizationalUnit( systemDefaults.config().configuration().supportOrganizationName().get() );
               }

               try
               {
                  project = ou.getProjectByName( systemDefaults.config().configuration().supportProjectForEmailName().get() );
               } catch (IllegalArgumentException iae)
               {
                  project = ou.createProject( systemDefaults.config().configuration().supportProjectForEmailName().get() );
               }

               try
               {
                  caseType = project.getCaseTypeByName( systemDefaults.config().configuration().supportCaseTypeForFailedEmailName().get() );
               } catch (IllegalArgumentException iae)
               {
                  caseType = ou.createCaseType( systemDefaults.config().configuration().supportCaseTypeForFailedEmailName().get() );
                  project.addSelectedCaseType( caseType );
                  project.addMember( RoleMap.current().get( Member.class ) );
               }
            } finally
            {
               uow.complete();
               RoleMap.clearCurrentRoleMap();
            }

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

         boolean expunge = config.configuration().deleteMailOnInboxClose().get();
         logger.info("Checking email");
         logger.info( "Delete mail on close - " + expunge );

         Session session = javax.mail.Session.getInstance(props, authenticator);
         session.setDebug(config.configuration().debug().get());

         Usecase usecase = newUsecase( "Receive Mail" );
         UnitOfWork uow = null;
         Store store = null;
         Folder inbox = null;
         Folder archive = null;
         boolean archiveExists = false;
         List<Message> copyToArchive = new ArrayList<Message>();

         try
         {
            store = session.getStore(url);
            store.connect();

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            javax.mail.Message[] messages = inbox.getMessages();
            FetchProfile fp = new FetchProfile();
//            fp.add( "In-Reply-To" );
            inbox.fetch(messages, fp);

            // check if the archive folder is configured and exists
            if( !Strings.empty( config.configuration().archiveFolder().get() )
                  && config.configuration().protocol().get().startsWith( "imap" ) )
            {
               archive = store.getFolder( config.configuration().archiveFolder().get() );

               // if not exists - create
               if( !archive.exists() )
               {
                  archive.create( Folder.HOLDS_MESSAGES );
                  archiveExists = true;
               } else
               {
                  archiveExists = true;
               }

               archive.open( Folder.READ_WRITE );
            }

            for (javax.mail.Message message : messages)
            {
               uow = module.unitOfWorkFactory().newUnitOfWork(usecase);

               ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder(EmailValue.class);

               try
               {
                  Object content = message.getContent();

                  // Get email fields
                  builder.prototype().from().set(((InternetAddress) message.getFrom()[0]).getAddress());
                  builder.prototype().fromName().set(((InternetAddress) message.getFrom()[0]).getPersonal());
                  builder.prototype().to().set(((InternetAddress) message.getRecipients(Message.RecipientType.TO)[0]).getAddress());
                  builder.prototype().subject().set(message.getSubject());

                  // Get headers
                  for (Header header : Iterables.iterable( (Enumeration<Header>) message.getAllHeaders() ))
                  {
                     builder.prototype().headers().get().put(header.getName(), header.getValue());
                  }

                  builder.prototype().messageId().set(message.getHeader("Message-ID")[0]);

                  // Get body and attachments
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
                           ValueBuilder<AttachedFileValue> attachmentBuilder = module.valueBuilderFactory().newValueBuilder(AttachedFileValue.class);

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
                           if (part.isMimeType("text/plain"))
                           {
                              body = (String) part.getContent();
                              builder.prototype().content().set(body);
                              builder.prototype().contentType().set(part.getContentType());
                           } else if (part.getContent() instanceof Multipart) {
                              Multipart bodyMultipart = (Multipart) part.getContent();
                              for (int j = 0, k = bodyMultipart.getCount(); j < k; j++)
                              {
                                 BodyPart bodyPart = bodyMultipart.getBodyPart(i);
                                 if (bodyPart.isMimeType("text/plain"))
                                 {
                                    body = (String) bodyPart.getContent();
                                    builder.prototype().content().set(body);
                                    builder.prototype().contentType().set(bodyPart.getContentType());
                                 }
                              }
                           }
                        }
                     }
                  } else if (content instanceof InputStream)
                  {
                     content = new MimeMessage(session, (InputStream) content).getContent();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     Inputs.byteBuffer((InputStream) content, 4096).transferTo(Outputs.byteBuffer(baos));

                     String data = new String(baos.toByteArray(), "UTF-8");
                     // Unknown content type - abort
                     // and create failure case
                     String subj = "Unkonwn content type: " + message.getSubject();
                     builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );
                     systemDefaults.createCaseOnEmailFailure( builder.newInstance() );
                     copyToArchive.add( message );

                     uow.discard();
                     continue;
                  }else
                  {
                     // Unknown content type - abort
                     // and create failure case
                     String subj = "Unkonwn content type: " + message.getSubject();
                     builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );
                     systemDefaults.createCaseOnEmailFailure(  builder.newInstance() );
                     copyToArchive.add( message );

                     uow.discard();
                     logger.error("Could not parse emails: unknown content type "+content.getClass().getName());
                     continue;
                  }

                  mailReceiver.receivedEmail(null, builder.newInstance());

                  uow.complete();

                  copyToArchive.add( message );
                  // remove mail on success if expunge is true
                  if( expunge )
                     message.setFlag( Flags.Flag.DELETED, true );

               } catch (Throwable e)
               {
                  String subj = "Unknown error: " + message.getSubject();
                  builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );
                  systemDefaults.createCaseOnEmailFailure( builder.newInstance() );
                  copyToArchive.add( message );

                  uow.discard();
                  logger.error("Could not parse emails", e);
               }
            }

            // copy message to archive if archive exists
            if( archiveExists )
            {
               inbox.copyMessages( copyToArchive.toArray( new Message[0] ), archive );
               archive.close( false );
            }

            inbox.close( config.configuration().deleteMailOnInboxClose().get() );

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