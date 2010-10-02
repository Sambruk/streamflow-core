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

import javax.management.ObjectName;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JAVADOC
 */
public class CircuitBreakers
{
   Map<CircuitBreaker, ObjectName> breakerMBeans = new HashMap<CircuitBreaker, ObjectName>( );

   List<CircuitBreakersListener> listeners = Collections.synchronizedList( new ArrayList<CircuitBreakersListener>( ));

   public CircuitBreaker createCircuitBreaker(String name, final CircuitTest circuitTest )
   {
      final CircuitBreaker circuitBreaker = new CircuitBreaker(name);
      circuitBreaker.addPropertyChangeListener( new PropertyChangeListener()
      {
         public void propertyChange( PropertyChangeEvent evt )
         {
            if (evt.getPropertyName().equals("enabled"))
            {
               try
               {
                  if (circuitBreaker.isEnabled())
                     circuitBreaker.setStatus( CircuitBreaker.Status.on );
               } catch (PropertyVetoException e)
               {
                  e.printStackTrace();
               }
            }
         }
      });

      circuitBreaker.addVetoableChangeListener( new VetoableChangeListener()
      {
         public void vetoableChange( PropertyChangeEvent evt ) throws PropertyVetoException
         {
            try
            {
               circuitTest.test();
            } catch (Exception e)
            {
               throw (PropertyVetoException) new PropertyVetoException( e.getMessage(), evt).initCause( e );
            }
         }
      });

      for (int i = 0; i < listeners.size(); i++)
      {
         CircuitBreakersListener circuitBreakersListener = listeners.get( i );
         circuitBreakersListener.addedCircuitBreaker( circuitBreaker );
      }

      return circuitBreaker;
   }

   public void removeCircuitBreaker(CircuitBreaker circuitBreaker)
   {
      for (int i = 0; i < listeners.size(); i++)
      {
         CircuitBreakersListener circuitBreakersListener = listeners.get( i );
         circuitBreakersListener.removedCircuitBreaker( circuitBreaker );
      }
   }
}
