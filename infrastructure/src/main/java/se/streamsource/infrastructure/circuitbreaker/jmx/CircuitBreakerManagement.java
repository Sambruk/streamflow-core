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
package se.streamsource.infrastructure.circuitbreaker.jmx;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.library.jmx.Qi4jMBeans;
import org.slf4j.LoggerFactory;
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;
import se.streamsource.infrastructure.circuitbreaker.service.ServiceCircuitBreaker;

import javax.management.AttributeChangeNotification;
import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * JMX service that exposes ServiceCircuitBreakers as MBeans.
 */
@Mixins(CircuitBreakerManagement.Mixin.class)
public interface CircuitBreakerManagement
      extends ServiceComposite, Activatable
{
   class Mixin
         implements Activatable
   {
      Map<CircuitBreaker, ObjectName> registeredCircuitBreakers = new HashMap<CircuitBreaker, ObjectName>( );

      @Structure
      Application application;

      @Service
      MBeanServer server;

      @Service
      Iterable<ServiceReference<ServiceCircuitBreaker>> circuitBreakers;

      public void activate() throws Exception
      {
         for (ServiceReference<ServiceCircuitBreaker> circuitBreaker : circuitBreakers)
         {
            registerCircuitBreaker( circuitBreaker.get().getCircuitBreaker(), circuitBreaker.identity() );
         }
      }

      public void passivate() throws Exception
      {
         for (ObjectName objectName : registeredCircuitBreakers.values())
         {
            server.unregisterMBean( objectName );
         }
         registeredCircuitBreakers.clear();
      }

      public void registerCircuitBreaker( final CircuitBreaker circuitBreaker, final String name ) throws JMException
      {
         ObjectName mbeanObjectName = null;

         ObjectName serviceName = Qi4jMBeans.findServiceName(server, application.name(), name);
         if (serviceName != null)
         {
            mbeanObjectName = new ObjectName(serviceName.toString()+",name=Circuit breaker");
         } else
         {
            try
            {
               mbeanObjectName = new ObjectName("CircuitBreaker:name=" + name );
            } catch (MalformedObjectNameException e)
            {
               throw new IllegalArgumentException("Illegal name:"+name);
            }
         }

         CircuitBreakerJMX bean = new CircuitBreakerJMX(circuitBreaker, name);

         try
         {
            server.registerMBean( bean, mbeanObjectName );
            registeredCircuitBreakers.put( circuitBreaker, mbeanObjectName );
         } catch (InstanceAlreadyExistsException e)
         {
            e.printStackTrace();
         } catch (MBeanRegistrationException e)
         {
            e.printStackTrace();
         } catch (NotCompliantMBeanException e)
         {
            e.printStackTrace();
         }

         // Add logger
         circuitBreaker.addPropertyChangeListener( new PropertyChangeListener()
         {
            public void propertyChange( PropertyChangeEvent evt )
            {
               if (evt.getPropertyName().equals( "status" ))
               {
                  if (evt.getNewValue().equals(CircuitBreaker.Status.on))
                  {
                     LoggerFactory.getLogger( CircuitBreakerManagement.class ).info( "Circuit breaker "+name+" is now on" );
                  }
                  else
                  {
                     LoggerFactory.getLogger( CircuitBreakerManagement.class ).error( "Circuit breaker "+name+" is now off" );
                  }
               }
            }
         });
      }
   }

}
