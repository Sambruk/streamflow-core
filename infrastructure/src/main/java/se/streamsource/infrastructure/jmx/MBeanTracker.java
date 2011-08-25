/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.infrastructure.jmx;

import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tracker that simplifies keeping track of MBeans in an MBeanServer
 * whose names matches a particular pattern. Callbacks are called
 * when MBeans are added/removed if their names match the given pattern.
 *
 * Example usage:
 * new MBeanTracker(new ObjectName("*:*,name=Circuit breaker"), server).registerCallback(updateStatus).start();
 */
public class MBeanTracker
{
   private boolean started = false;
   private ObjectName pattern;
   private MBeanServer server;
   private List<TrackerCallback> callbacks = new ArrayList<TrackerCallback>();
   private List<ObjectName> tracked = Collections.synchronizedList(new ArrayList<ObjectName>());
   private RegistrationListener registrationListener;

   public MBeanTracker(ObjectName pattern, MBeanServer server)
   {
      this.pattern = pattern;
      this.server = server;
   }

   public MBeanTracker registerCallback(TrackerCallback callback)
   {
      List<TrackerCallback> newList = new ArrayList<TrackerCallback>(callbacks);
      newList.add(callback);
      callbacks = newList;
      return this;
   }

   public MBeanTracker unregisterCallback(TrackerCallback callback)
   {
      List<TrackerCallback> newList = new ArrayList<TrackerCallback>(callbacks);
      newList.remove(callback);
      callbacks = newList;
      return this;
   }

   public Iterable<ObjectName> getTracked()
   {
      return tracked;
   }

   public void start()
   {
      if (!started)
      {
         try
         {
            registrationListener = new RegistrationListener();
            server.addNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), registrationListener, null, null);

            for (ObjectName objectName : server.queryNames(pattern, null))
            {
               for (TrackerCallback callback : callbacks)
               {
                  try
                  {
                     tracked.add(objectName);
                     callback.addedMBean(objectName, server);
                  } catch (Throwable throwable)
                  {
                     LoggerFactory.getLogger(MBeanTracker.class).error("Tracker callback threw exception", throwable);
                  }
               }
            }

            started = true;
         } catch (Exception e)
         {
            // Could not start
         }
      }
   }

   public void stop()
   {
      if (started)
      {
         try
         {
            server.removeNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), registrationListener);

            for (ObjectName objectName : server.queryNames(pattern, null))
            {
               for (TrackerCallback callback : callbacks)
               {
                  try
                  {
                     callback.removedMBean(objectName, server);
                     tracked.remove(objectName);
                  } catch (Throwable throwable)
                  {
                     LoggerFactory.getLogger(MBeanTracker.class).error("Tracker callback threw exception", throwable);
                  }
               }
            }

            started = false;
         } catch (Exception e)
         {
            // Could not start
         }
      }
   }

   public interface TrackerCallback
   {
      void addedMBean(ObjectName newMBean, MBeanServer server)
         throws Throwable;

      void removedMBean(ObjectName removedMBean, MBeanServer server)
         throws Throwable;
   }

   class RegistrationListener
      implements NotificationListener
   {
      public void handleNotification(Notification notification, Object o)
      {
         if (notification instanceof MBeanServerNotification)
         {
            MBeanServerNotification serverNotification = (MBeanServerNotification) notification;
            if (pattern.apply(serverNotification.getMBeanName()))
            {
               if (serverNotification.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
               {
                  for (TrackerCallback callback : callbacks)
                  {
                     try
                     {
                        tracked.add(serverNotification.getMBeanName());
                        callback.addedMBean(serverNotification.getMBeanName(), server);
                     } catch (Throwable throwable)
                     {
                        LoggerFactory.getLogger(MBeanTracker.class).error("Tracker callback threw exception", throwable);
                     }
                  }
               } else if (serverNotification.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
               {
                  for (TrackerCallback callback : callbacks)
                  {
                     try
                     {
                        callback.removedMBean(serverNotification.getMBeanName(), server);
                        tracked.remove(serverNotification.getMBeanName());
                     } catch (Throwable throwable)
                     {
                        LoggerFactory.getLogger(MBeanTracker.class).error("Tracker callback threw exception", throwable);
                     }
                  }
               }
            }
         }
      }
   }
}
