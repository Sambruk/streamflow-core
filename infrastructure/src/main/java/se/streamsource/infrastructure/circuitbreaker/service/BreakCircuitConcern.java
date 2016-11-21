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

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.injection.scope.This;
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;

import java.lang.reflect.Method;

/**
 * This concern will update the circuit breaker on method invocation success
 * and thrown exceptions.
 */
@AppliesTo(BreaksCircuitOnThrowable.class)
public class BreakCircuitConcern
   extends GenericConcern
{
   @This ServiceCircuitBreaker serviceCircuitBreaker;

   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
   {
      CircuitBreaker circuitBreaker = serviceCircuitBreaker.getCircuitBreaker();
      try
      {
         if (!circuitBreaker.isOn())
            throw circuitBreaker.getLastThrowable();

         Object result = next.invoke( proxy, method, args );
         circuitBreaker.success();
         return result;
      } catch (Throwable throwable)
      {
         circuitBreaker.throwable( throwable );
         throw throwable;
      }
   }
}
