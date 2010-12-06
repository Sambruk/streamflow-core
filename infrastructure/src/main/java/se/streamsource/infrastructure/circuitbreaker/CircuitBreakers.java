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

package se.streamsource.infrastructure.circuitbreaker;

import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;

/**
 * CircuitBreaker helper methods.
 */
public class CircuitBreakers
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
}
