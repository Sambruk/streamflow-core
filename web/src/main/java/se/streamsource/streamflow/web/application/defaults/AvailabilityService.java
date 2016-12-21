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
package se.streamsource.streamflow.web.application.defaults;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.service.ServiceDescriptor;

import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;
import se.streamsource.infrastructure.circuitbreaker.service.AbstractEnabledCircuitBreakerAvailability;
import se.streamsource.infrastructure.circuitbreaker.service.ServiceCircuitBreaker;

/**
 * A service used to flag if Streamflow( server ) is available or not.
 * Flag is handled by a circuit breaker.
 */
@Mixins(AvailabilityService.Mixin.class)
public interface AvailabilityService
   extends Configuration, ServiceComposite, Activatable, ServiceCircuitBreaker, AbstractEnabledCircuitBreakerAvailability
{

   class Mixin
      implements Activatable, ServiceCircuitBreaker
   {
      @This
      Configuration<AvailabilityConfiguration> config;

      @Uses
      ServiceDescriptor descriptor;

      private CircuitBreaker circuitBreaker;

      public void activate() throws Exception
      {
         // Read arbitrary property just to activate config-handler
         config.configuration().enabled();
         circuitBreaker = descriptor.metaInfo(CircuitBreaker.class);
         circuitBreaker.turnOn();
      }

      public void passivate() throws Exception
      {

      }

      public CircuitBreaker getCircuitBreaker()
      {
         return circuitBreaker;
      }
   }
}
