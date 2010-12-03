/*
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

package se.streamsource.streamflow.web.infrastructure.circuitbreaker;

import java.beans.PropertyVetoException;
import java.text.DateFormat;
import java.util.Date;

/**
* JAVADOC
*/
public class CircuitBreakerJMX
   implements CircuitBreakerJMXMBean
{
   CircuitBreaker circuitBreaker;

   public CircuitBreakerJMX( CircuitBreaker circuitBreaker )
   {
      this.circuitBreaker = circuitBreaker;
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
      return circuitBreaker.getLastThrowable() == null ? "" : circuitBreaker.getLastThrowable().getMessage();
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
