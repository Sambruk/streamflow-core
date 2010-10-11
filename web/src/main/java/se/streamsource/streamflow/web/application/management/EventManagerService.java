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

package se.streamsource.streamflow.web.application.management;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.RequiredModelMBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for exposing domain events through JMX. Allows all domain events to be listened to
 * as JMX Notifications. The event name is set as notification name, and the JSON serialization of
 * the event is used as user date in the notifications.
 */
@Mixins(EventManagerService.Mixin.class)
public interface EventManagerService
      extends Activatable, ServiceComposite
{
   class Mixin
         implements Activatable, TransactionVisitor
   {
      @Service
      EventSource source;

      @Service
      MBeanServer server;
      public ObjectName objectName;

      long seq = 0;
      public RequiredModelMBean mbean;
      ExecutorService executor;

      public void activate() throws Exception
      {
         ModelMBeanNotificationInfo[] notificationInfos = new ModelMBeanNotificationInfo[1];
         notificationInfos[0] = new ModelMBeanNotificationInfo( new String[]{"domainevent"}, "Domain events", "Domain events" );
         ModelMBeanInfo info = new ModelMBeanInfoSupport( EventManagerService.class.getName(),
               "Domain events",
               null, null, null, notificationInfos );
         mbean = new RequiredModelMBean( info );

         objectName = new ObjectName( "Streamflow:type=Log,name=domainevents" );
         server.registerMBean( mbean, objectName );

         source.registerListener( this );

         executor = Executors.newSingleThreadExecutor();
      }

      public void passivate() throws Exception
      {
         executor.shutdown();

         server.unregisterMBean( objectName );
         source.unregisterListener( this );
      }

      public final synchronized boolean visit( TransactionEvents transaction )
      {
         for (DomainEvent domainEvent : Events.events( transaction ))
         {
            final Notification notification = new Notification( "domainevent", objectName, seq++, domainEvent.on().get().getTime(), domainEvent.name().get() );
            notification.setUserData( domainEvent.toJSON() );

            executor.submit( new Runnable()
            {
               public void run()
               {
                  try
                  {
                     mbean.sendNotification( notification );
                  } catch (MBeanException e)
                  {
                     e.printStackTrace();
                  }
               }
            } );

         }

         return true;
      }
   }

}
