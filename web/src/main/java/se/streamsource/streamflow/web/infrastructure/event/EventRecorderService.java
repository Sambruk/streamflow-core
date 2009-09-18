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

package se.streamsource.streamflow.web.infrastructure.event;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.json.JSONObject;

import java.io.*;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Records events to disk, and allows them to be replayed
 */
@Mixins({EventRecorderService.EventRecorderMixin.class, EventRecorderService.EventReplayMixin.class})
public interface EventRecorderService
        extends EventSourceListener, EventReplay, ServiceComposite, Activatable
{
    class EventRecorderMixin
            implements EventSourceListener, Activatable
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Service
        FileConfiguration config;
        private File eventDir;

        @Service
        EventSource source;

        private BufferedWriter out;
        public Logger logger;

        public void activate() throws Exception
        {
            logger = Logger.getLogger(getClass().getName());
            eventDir = new File(config.dataDirectory(), "events");
            if (!eventDir.mkdirs())
                throw new IllegalStateException("Could not create directory for event data");

//                source.registerListener(this);
        }

        public void passivate() throws Exception
        {
            source.unregisterListener(this);

            if (out != null)
            {
                out.close();
            }
        }

        public void eventsAvailable(EventStore eventStore, EventSpecification specification)
        {
            Iterable<DomainEvent> events = eventStore.events(specification, null, 100);

            try
            {
                if (out == null)
                {
                    out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(eventDir, "events.json"), true)));
                }

                for (DomainEvent domainEvent : events)
                {
                    out.append(domainEvent.toJSON()).append('\n');
                }

                out.flush();
            } catch (IOException e)
            {
                logger.log(Level.SEVERE, "Could not store events", e);

            }
        }
    }

    class EventReplayMixin
            implements EventReplay
    {
        @Structure
        UnitOfWorkFactory uowf;

        public void replayEvent(DomainEvent event)
                throws Exception
        {
            UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase(event.name().get()));
            String identity = event.entity().get();
            Object entity = uow.get(Object.class, identity);
            String name = event.name().get();
            Method eventMethod = null;
            for (Method method : entity.getClass().getMethods())
            {
                if (method.getName().equals(name))
                {
                    eventMethod = method;
                    break;
                }
            }

            if (eventMethod == null)
                throw new IllegalArgumentException("No such event method:"+name);

            JSONObject jsonObject = new JSONObject(event.parameters().get());
            Object[] params = new Object[eventMethod.getParameterTypes().length];
            params[0] = event;
            for (int idx = 1; idx < params.length; idx++)
            {
                String paramName = "param" + idx;
                params[idx] = jsonObject.get(paramName);
            }

            eventMethod.invoke(entity, params);

            uow.complete();
        }
    }
}
