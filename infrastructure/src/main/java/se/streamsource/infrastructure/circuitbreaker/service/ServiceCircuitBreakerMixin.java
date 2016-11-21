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

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.spi.service.ServiceDescriptor;
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;

/**
 * Helper implementation of ServiceCircuitBreaker. Fetches the CircuitBreaker from meta-info
 * for the service.
 */
public class ServiceCircuitBreakerMixin
   implements ServiceCircuitBreaker, Initializable
{
   @Uses
   ServiceDescriptor descriptor;

   CircuitBreaker circuitBreaker;

   public void initialize() throws InitializationException
   {
      circuitBreaker = descriptor.metaInfo( CircuitBreaker.class );
   }

   public CircuitBreaker getCircuitBreaker()
   {
      return circuitBreaker;
   }
}
