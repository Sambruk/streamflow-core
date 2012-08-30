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
package se.streamsource.streamflow.web.application.channel;

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
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import static se.streamsource.infrastructure.circuitbreaker.CircuitBreakers.*;

/**
 * Send messages on integration channel. This service
 * listens for application events, and on "sentChannelMessage" it will send
 * the provided ChannelMessageValue.
 */
@Mixins(SendChannelService.Mixin.class)
public interface SendChannelService
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

      @org.qi4j.api.injection.scope.Service
      AttachmentStore attachmentStore;

      @This
      Configuration<SendChannelServiceConfiguration> config;

      @Uses
      ServiceDescriptor descriptor;

      public Logger logger;

      ApplicationTransactionTracker<ApplicationEventReplayException> tracker;
      private CircuitBreaker circuitBreaker;

      public void activate() throws Exception
      {
         logger = LoggerFactory.getLogger( SendChannelService.class );

         circuitBreaker = descriptor.metaInfo( CircuitBreaker.class );
         tracker = new ApplicationTransactionTracker<ApplicationEventReplayException>( stream,
               source,
               config,
               withBreaker( circuitBreaker,
                     ApplicationEvents.playEvents( player, new SendChannelMessages() ) ));

         if (config.configuration().enabled().get())
         {
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

      public class SendChannelMessages
            implements ChannelSender
      {
         public void sentChannelMessage( ApplicationEvent event, ChannelMessageValue message )
         {
            try
            {

               logger.debug( "Sent channel message to " );
            } catch (Throwable e)
            {
               throw new ApplicationEventReplayException( event, e );
            }
         }
      }
   }
}