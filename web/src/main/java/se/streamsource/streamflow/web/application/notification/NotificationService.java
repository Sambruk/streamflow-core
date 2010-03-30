/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.application.notification;

import org.qi4j.api.Qi4j;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventCollector;
import se.streamsource.streamflow.infrastructure.event.source.EventQuery;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.infrastructure.event.source.TransactionEventAdapter;
import se.streamsource.streamflow.infrastructure.event.source.TransactionTimestampFilter;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;
import se.streamsource.streamflow.web.application.mail.MailService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Send and receive notifications. This service
 * listens for domain events, and on "receivedMessage" it will send
 * a notification to the provided recipient.
 */
@Mixins(NotificationService.Mixin.class)
public interface NotificationService
      extends TransactionVisitor, Configuration, Activatable, ServiceComposite
{
   class Mixin
         implements TransactionVisitor, Activatable
   {
      @Structure
      Qi4j api;

      @Service
      EventStore eventStore;

      @Service
      EventSource source;

      @Service
      MailService mail;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Configuration<NotificationConfiguration> config;

      public Logger logger;

      private EventSpecification userNotificationFilter;

      private Usecase usecase = UsecaseBuilder.newUsecase( "Notify" );

      public void activate() throws Exception
      {
         logger = Logger.getLogger( NotificationService.class.getName() );

         logger.info( "Starting ..." );
         try
         {
            source.registerListener( this );
         } catch (Exception e)
         {
            e.printStackTrace();
            throw e;
         }


         userNotificationFilter = new EventQuery().withNames( "receivedMessage" );

         visit( null ); // Trigger a load
         logger.info( "Started" );
      }

      public void passivate() throws Exception
      {
         source.unregisterListener( this );
      }

      public boolean visit( TransactionEvents transaction )
      {
         if (config.configuration().enabled().get())
         {
            TransactionTimestampFilter timestamp;
            EventCollector eventCollector;
            eventStore.transactionsAfter( config.configuration().lastEventDate().get(),
                  timestamp = new TransactionTimestampFilter( config.configuration().lastEventDate().get(),
                        new TransactionEventAdapter(
                              new EventVisitorFilter( userNotificationFilter, eventCollector = new EventCollector() ) ) ) );

            // Handle all receivedMessage events
            if (!eventCollector.events().isEmpty())
            {
               UnitOfWork uow = null;

               try
               {
                  uow = uowf.newUnitOfWork( usecase );

                  for (DomainEvent domainEvent : eventCollector.events())
                  {
                     UserEntity user = uow.get( UserEntity.class, domainEvent.entity().get());

                     if( ((MessageRecipient.Data)user).delivery().get().equals( MessageRecipient.MessageDeliveryTypes.email))
                     {
                        mail.sendNotification( domainEvent );
                     }
                  }

                  config.configuration().lastEventDate().set( timestamp.lastTimestamp() );
                  config.save();
               } catch (Exception e)
               {
                  logger.log( Level.SEVERE, "Could not send notification", e );
               } finally
               {
                  uow.discard();
               }
            }
         }

         return true;
      }
   }
}