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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;
import se.streamsource.streamflow.infrastructure.event.source.helper.TransactionTracker;
import se.streamsource.streamflow.util.Specification;
import se.streamsource.streamflow.web.application.mail.MailService;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;

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
      final Logger logger = LoggerFactory.getLogger( NotificationService.class.getName() );
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

      private Specification userNotificationFilter;

      private Usecase usecase = UsecaseBuilder.newUsecase( "Notify" );
      public TransactionVisitor subscriber;

      private TransactionTracker tracker;

      public void activate() throws Exception
      {
         logger.info( "Starting ..." );

         userNotificationFilter = Events.withNames( "receivedMessage" );

         tracker = new TransactionTracker( eventStore, config, this );
         tracker.start();

         logger.info( "Started" );
      }

      public void passivate() throws Exception
      {
         tracker.stop();
      }

      public boolean visit( TransactionEvents transaction )
      {
         return Events.adapter( 
               new EventVisitorFilter( userNotificationFilter, new EventVisitor()
               {
                  public boolean visit( DomainEvent event )
                  {
                     UnitOfWork uow = null;

                     try
                     {
                        uow = uowf.newUnitOfWork( usecase );

                        UserEntity user = uow.get( UserEntity.class, event.entity().get() );

                        if (user.delivery().get().equals( MessageRecipient.MessageDeliveryTypes.email ))
                        {
                           mail.sendNotification( event );
                        }
                     } catch (Exception e)
                     {
                        logger.error( "Could not send notification", e );

                        return false;
                     } finally
                     {
                        uow.discard();
                     }

                     return true;
                  }
               } )).visit( transaction );
      }
   }
}