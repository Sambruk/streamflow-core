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
package se.streamsource.infrastructure.circuitbreaker.service;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Availability;
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;

/**
 * Abstract composite that determines Availability by
 * checking the Enabled configuration and a CircuitBreaker.
 *
 * To use this, the service must implement ServiceCircuitBreaker, and its ConfigurationComposite
 * must extend Enabled.
 */
@Mixins(AbstractEnabledCircuitBreakerAvailability.Mixin.class)
public interface AbstractEnabledCircuitBreakerAvailability
   extends Availability
{
   class Mixin
      implements Availability
   {
      @This
      Configuration<Enabled> config;

      @This
      ServiceCircuitBreaker circuitBreaker;

      public boolean isAvailable()
      {
         return config.configuration().enabled().get() && circuitBreaker.getCircuitBreaker().getStatus() == CircuitBreaker.Status.on;
      }
   }
}
