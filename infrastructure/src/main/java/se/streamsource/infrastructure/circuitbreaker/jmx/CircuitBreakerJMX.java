/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.infrastructure.circuitbreaker.jmx;

import org.slf4j.LoggerFactory;
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.text.DateFormat;
import java.util.Date;

/**
* MBean for circuit breakers
*/
public class CircuitBreakerJMX
   extends NotificationBroadcasterSupport
   implements CircuitBreakerJMXMBean
{
   CircuitBreaker circuitBreaker;

   long seqNr = 1;

   public CircuitBreakerJMX(final CircuitBreaker circuitBreaker, final String name)
   {
      super(new MBeanNotificationInfo(new String[]{AttributeChangeNotification.ATTRIBUTE_CHANGE}, AttributeChangeNotification.class.getName(), "CircuitBreaker status has changed"));
      this.circuitBreaker = circuitBreaker;

      circuitBreaker.addPropertyChangeListener( new PropertyChangeListener()
      {
         public void propertyChange( PropertyChangeEvent evt )
         {
            if (evt.getPropertyName().equals( "status" ))
            {
               if (evt.getNewValue().equals(CircuitBreaker.Status.on))
               {
                  sendNotification(new AttributeChangeNotification(CircuitBreakerJMX.this, seqNr++, System.currentTimeMillis(), "Status changed for "+name, "Status", String.class.getName(), CircuitBreaker.Status.off, CircuitBreaker.Status.on));
               }
               else
               {
                  sendNotification(new AttributeChangeNotification(CircuitBreakerJMX.this, seqNr++, System.currentTimeMillis(), "Status changed for "+name, "Status", String.class.getName(), CircuitBreaker.Status.on, CircuitBreaker.Status.off));
               }
            }
         }
      });
   }

   public String getStatus()
   {
      return circuitBreaker.getStatus().name();
   }

   public int getThreshold()
   {
      return circuitBreaker.getThreshold();
   }

   public double getServiceLevel()
   {
      return circuitBreaker.getServiceLevel();
   }

   public String getLastErrorMessage()
   {
      return circuitBreaker.getLastThrowable() == null ? "" : errorMessage(circuitBreaker.getLastThrowable());
   }

   private String errorMessage( Throwable throwable )
   {
      String message = throwable.getMessage();
      if (message == null)
         message = throwable.getClass().getSimpleName();

      if (throwable.getCause() != null)
      {
         return message + ":" + errorMessage( throwable.getCause() );
      } else
         return message;
   }

   public String getTrippedOn()
   {
      Date trippedOn = circuitBreaker.getTrippedOn();
      return trippedOn == null ? "" : DateFormat.getDateTimeInstance().format( trippedOn );
   }

   public String getEnableOn()
   {
      Date trippedOn = circuitBreaker.getEnableOn();
      return trippedOn == null ? "" : DateFormat.getDateTimeInstance().format( trippedOn );
   }

   public String turnOn()
   {
      try
      {
         circuitBreaker.turnOn();
         return "Circuit breaker has been turned on";
      } catch (PropertyVetoException e)
      {
         return "Could not turn on circuit breaker:"+getLastErrorMessage();
      }
   }

   public void trip()
   {
      circuitBreaker.trip();
   }
}
