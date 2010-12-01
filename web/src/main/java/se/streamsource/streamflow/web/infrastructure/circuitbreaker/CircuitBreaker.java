/**
 *
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

import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;

import java.beans.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of CircuitBreaker pattern
 */
public class CircuitBreaker
{
   public static <Item, ReceiverThrowable extends Throwable> Output<Item, ReceiverThrowable> withBreaker( final CircuitBreaker breaker, final Output<Item, ReceiverThrowable> output)
   {
      return new Output<Item, ReceiverThrowable>()
      {
         public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<Item, SenderThrowableType> sender ) throws ReceiverThrowable, SenderThrowableType
         {
            output.receiveFrom( new Sender<Item, SenderThrowableType>()
            {
               public <ReceiverThrowableType extends Throwable> void sendTo( final Receiver<Item, ReceiverThrowableType> receiver ) throws ReceiverThrowableType, SenderThrowableType
               {
                  // Check breaker first
                  if (!breaker.isOn())
                     throw (ReceiverThrowableType) breaker.getLastThrowable();

                  sender.sendTo( new Receiver<Item, ReceiverThrowableType>()
                  {
                     public void receive( Item item ) throws ReceiverThrowableType
                     {
                        try
                        {
                           receiver.receive( item );

                           // Notify breaker that it went well
                           breaker.success();
                        } catch (Throwable receiverThrowableType)
                        {
                           // Notify breaker of trouble
                           breaker.throwable( receiverThrowableType );

                           throw (ReceiverThrowableType) receiverThrowableType;
                        }
                     }
                  });
               }
            });
         }
      };
   }

   public enum Status
   {
      off,
      on
   }

   private int threshold;
   private long timeout;
   private Set<Class<? extends Exception>> allowedExceptions = new HashSet<Class<? extends Exception>>();

   private int countDown;
   private long trippedOn;
   private long enableOn;

   private Status status = Status.on;

   private Throwable lastThrowable;

   PropertyChangeSupport pcs = new PropertyChangeSupport(this);
   VetoableChangeSupport vcs = new VetoableChangeSupport(this);

   public CircuitBreaker( int threshold, long timeout, Class<? extends Exception>... allowedExceptions)
   {
      this.threshold = threshold;
      this.countDown = threshold;
      this.timeout = timeout;
      Collections.addAll( this.allowedExceptions, allowedExceptions );
   }

   public CircuitBreaker(Class<? extends Exception>... allowedExceptions)
   {
      this(1, 1000*60*5); // 5 minute timeout as default
   }

   public synchronized void trip()
   {
      if (status == Status.on)
      {
         status = Status.off;
         pcs.firePropertyChange( "status", Status.on, Status.off );

         trippedOn = System.currentTimeMillis();
         enableOn = trippedOn+timeout;
      }
   }

   public synchronized void turnOn() throws PropertyVetoException
   {
      if (status == Status.off)
      {
         try
         {
            vcs.fireVetoableChange( "status", Status.off, Status.on );
            status = Status.on;
            countDown = threshold;
            enableOn = 0;

            pcs.firePropertyChange( "status", Status.off, Status.on );
         } catch (PropertyVetoException e)
         {
            // Reset timeout
            enableOn = enableOn+timeout;

            if (e.getCause() != null)
               lastThrowable = e.getCause();
            throw e;
         }
      }
   }

   public synchronized Throwable getLastThrowable()
   {
      return lastThrowable;
   }

   public synchronized double getServiceLevel()
   {
      return countDown/((double)threshold);
   }

   public synchronized Status getStatus()
   {
      if (status == Status.off)
      {
         if (System.currentTimeMillis() > enableOn)
         {
            try
            {
               turnOn();
            } catch (PropertyVetoException e)
            {
               if (e.getCause() != null)
                  lastThrowable = e.getCause();
            }
         }
      }

      return status;
   }

   public boolean isOn()
   {
      return getStatus().equals( Status.on );
   }

   public synchronized void throwable(Throwable throwable)
   {
      Class<? extends Throwable> exceptionClass = throwable.getClass();
      if ( status == Status.on && !allowedExceptions.contains( exceptionClass ))
      {
         // Check if exception is subclass of allowed exception, and if so, add it to list
         if (Exception.class.isAssignableFrom( exceptionClass))
         {
            for (Class<? extends Exception> allowedException : allowedExceptions)
            {
               if (allowedException.isAssignableFrom( exceptionClass ));
               {
                  allowedExceptions.add( (Class<Exception>) exceptionClass );
                  return;
               }
            }
         }

         countDown--;

         lastThrowable = throwable;

         pcs.firePropertyChange( "serviceLevel", (countDown+1)/((double)threshold), countDown/((double)threshold) );

         if (countDown == 0)
         {
            trip();
         }
      }
   }

   public synchronized void success()
   {
      if (status == Status.on && countDown < threshold)
      {
         countDown++;

         pcs.firePropertyChange( "serviceLevel", (countDown-1)/((double)threshold), countDown/((double)threshold) );
      }
   }

   public void addVetoableChangeListener( VetoableChangeListener vcl)
   {
      vcs.addVetoableChangeListener( vcl );
   }

   public void removeVetoableChangeListener( VetoableChangeListener vcl)
   {
      vcs.removeVetoableChangeListener( vcl );
   }

   public void addPropertyChangeListener( PropertyChangeListener pcl)
   {
      pcs.addPropertyChangeListener( pcl );
   }

   public void removePropertyChangeListener( PropertyChangeListener pcl)
   {
      pcs.removePropertyChangeListener( pcl );
   }
}
