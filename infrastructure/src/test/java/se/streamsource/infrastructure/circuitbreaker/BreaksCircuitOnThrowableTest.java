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
package se.streamsource.infrastructure.circuitbreaker;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import se.streamsource.infrastructure.circuitbreaker.service.AbstractBreakOnThrowable;
import se.streamsource.infrastructure.circuitbreaker.service.BreaksCircuitOnThrowable;
import se.streamsource.infrastructure.circuitbreaker.service.ServiceCircuitBreaker;

import java.beans.PropertyVetoException;

/**
 * Test @BreaksCircuitOnThrowable annotation
 */
public class BreaksCircuitOnThrowableTest
   extends AbstractQi4jTest
{

   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.services( TestService.class ).setMetaInfo( new CircuitBreaker() );
   }

   @Test
   public void testSuccess()
   {
      TestService service = (TestService) serviceLocator.findService( TestService.class ).get();
      service.success();
      service.success();
      service.success();
   }

   @Test
   public void testThrowable()
   {
      ServiceReference<TestService> serviceReference = serviceLocator.findService( TestService.class );
      TestService service = serviceReference.get();
      try
      {
         service.throwable();
         Assert.fail( "Service should have thrown exception" );
      } catch (Exception e)
      {
         // Ok
      }

      try
      {
         service.success();
         Assert.fail( "Circuit breaker should have tripped" );
      } catch (Exception e)
      {
         // Ok
      }

      try
      {
         serviceReference.metaInfo( CircuitBreaker.class ).turnOn();
      } catch (PropertyVetoException e)
      {
         Assert.fail("Should have been possible to turn on circuit breaker");
      }

      try
      {
         service.success();
      } catch (Exception e)
      {
         Assert.fail( "Circuit breaker should have been turned on" );
      }
   }

   @Mixins(TestService.Mixin.class)
   public interface TestService
      extends ServiceCircuitBreaker, AbstractBreakOnThrowable, ServiceComposite
   {
      int success();

      void throwable();

      abstract class Mixin
         implements TestService
      {
         int count = 0;

         @BreaksCircuitOnThrowable
         public void throwable()
         {
            throw new IllegalArgumentException("Failed");
         }

         @BreaksCircuitOnThrowable
         public int success()
         {
            return count++;
         }
      }
   }
}
