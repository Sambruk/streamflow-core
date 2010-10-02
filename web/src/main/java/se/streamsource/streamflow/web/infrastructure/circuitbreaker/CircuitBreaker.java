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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

/**
 * JAVADOC
 */
public class CircuitBreaker
{
   enum Status
   {
      off,
      on
   }

   private boolean enabled;
   private String name;


   private Status status = Status.off;
   private String errorMessage;

   PropertyChangeSupport pcs;
   VetoableChangeSupport vcs;

   public CircuitBreaker( String name)
   {
      this.name = name;
   }

   public boolean isEnabled()
   {
      return enabled;
   }

   public void setEnabled(boolean newEnabled)
   {
      if (this.enabled != enabled)
      {
         boolean oldEnabled = enabled;
         enabled = newEnabled;
         pcs.firePropertyChange( "enabled", oldEnabled, newEnabled );
      }
   }

   public String getName()
   {
      return name;
   }


   public void setStatus(Status newStatus) throws PropertyVetoException
   {
      if (newStatus != status)
      {
         if (newStatus == Status.on)
         {
            try
            {
               vcs.fireVetoableChange( "status", Status.off, Status.on );
            } catch (PropertyVetoException e)
            {
               errorMessage = e.getMessage();
               throw e;
            }
         }

         Status oldStatus = status;
         status = newStatus;
         pcs.firePropertyChange( "status", oldStatus, newStatus );
      }
   }

   public String getErrorMessage()
   {
      return errorMessage;
   }

   public Status getStatus()
   {
      return status;
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
