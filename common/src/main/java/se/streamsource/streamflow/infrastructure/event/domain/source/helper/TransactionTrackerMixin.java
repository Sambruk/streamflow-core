/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.infrastructure.event.domain.source.helper;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.usecase.Usecase;
import se.streamsource.streamflow.infrastructure.event.domain.replay.DomainEventPlayer;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;

/**
 * TODO
 */
public class TransactionTrackerMixin
      implements Activatable
{
      @Service
      DomainEventPlayer player;

      @Service
      private EventSource eventSource;

      @Service
      private EventStream stream;

      @Structure
      private Module module;

      @This
      private Configuration<TransactionTrackerConfiguration> config;

      @This
      private ServiceComposite self;

      private TransactionTracker tracker;

      public void activate() throws Exception
      {
         EventRouter router = new EventRouter();

         router.route( Events.withNames(self.getClass()), Events.playEvents( player, self, module.unitOfWorkFactory(), Usecase.DEFAULT) );

         tracker = new TransactionTracker( stream, eventSource, config, Events.adapter( router ) );
         tracker.start();
      }

      public void passivate() throws Exception
      {
         tracker.stop();
      }
}
