/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.application.management;

import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;

import javax.management.*;
import javax.management.modelmbean.RequiredModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;

import se.streamsource.streamflow.infrastructure.event.source.*;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(EventManagerService.EventManagerMixin.class)
public interface EventManagerService
    extends Activatable, ServiceComposite
{
    class EventManagerMixin
        implements Activatable, EventSourceListener
    {
        @Service
        EventSource source;

        @Service
        MBeanServer server;
        public ObjectName objectName;

        long seq = 0;
        public RequiredModelMBean mbean;

        public void activate() throws Exception
        {
            ModelMBeanNotificationInfo[] notificationInfos = new ModelMBeanNotificationInfo[1];
            notificationInfos[0] = new ModelMBeanNotificationInfo(new String[]{"domainevent"},"Domain events", "Domain events");
            ModelMBeanInfo info = new ModelMBeanInfoSupport(EventManagerService.class.getName(),
                    "Domain events",
                    null, null, null, notificationInfos);
            mbean = new RequiredModelMBean(info);

            objectName = new ObjectName("StreamFlow:name=domainevents");
            server.registerMBean(mbean, objectName);

            source.registerListener(this, AllEventsSpecification.INSTANCE);
        }

        public void passivate() throws Exception
        {
            server.unregisterMBean(objectName);
            source.unregisterListener(this);
        }

        public synchronized void eventsAvailable(EventStore source, EventSpecification specification)
        {
            Iterable<DomainEvent> events = source.events(specification, null, Integer.MAX_VALUE);

            for (DomainEvent event : events)
            {
                Notification notification = new Notification("domainevent", objectName, seq++, event.on().get().getTime(), event.name().get());
                notification.setUserData(event.toJSON());
                try
                {
                    mbean.sendNotification(notification);
                } catch (MBeanException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

}
