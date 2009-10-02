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

package se.streamsource.streamflow.infrastructure.event;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.json.JSONException;
import se.streamsource.streamflow.infrastructure.json.JSONObject;
import se.streamsource.streamflow.infrastructure.json.JSONStringer;
import se.streamsource.streamflow.infrastructure.json.JSONWriter;

import javax.security.auth.Subject;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.Principal;
import java.util.Date;
import java.util.Iterator;

/**
 * JAVADOC
 */
@Mixins(EventsService.EventsMixin.class)
public interface EventsService
        extends Events, ServiceComposite
{
    class EventsMixin
            implements Events
    {
        @Service
        Iterable<EventListener> listeners;

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        @Service
        IdentityGenerator idGenerator;

        @Service
        EventStore eventStore;

        @Structure
        Module module;

        String
        version;

        public void init(@Structure Application application)
        {
            version = application.version();
        }

        public DomainEvent createEvent( EntityComposite entity, String name, Object[] args )
        {
            ValueBuilder<DomainEvent> builder = vbf.newValueBuilder( DomainEvent.class );

            DomainEvent prototype = builder.prototype();
            prototype.name().set( name );
            prototype.entityType().set( entity.type().getName() );
            prototype.on().set( new Date() );
            prototype.entity().set( entity.identity().get() );

            Subject subject = Subject.getSubject( AccessController.getContext() );
            if (subject == null)
                prototype.by().set( "unknown" );
            else
            {
                Iterator<Principal> iterator = subject.getPrincipals().iterator();
                if (iterator.hasNext())
                    prototype.by().set( iterator.next().getName() );
                else
                    prototype.by().set( "unknown" );
            }

            prototype.identity().set( idGenerator.generate( DomainEvent.class ) );
            prototype.usecase().set( uowf.currentUnitOfWork().usecase().name() );
            prototype.version().set( version );

            // JSON-ify parameters
            JSONStringer json = new JSONStringer();
            try
            {
                JSONWriter params = json.object();
                for (int i = 1; i < args.length; i++)
                {
                    params.key( "param" + i );
                    if (args == null)
                        params.value( JSONObject.NULL );
                    else
                        params.value( args[i] );
                }
                json.endObject();
            } catch (JSONException e)
            {
                throw new IllegalArgumentException("Could not create event", e);
            }

            prototype.parameters().set( json.toString() );

            DomainEvent event = builder.newInstance();
            return event;
        }

        public void notifyEvent( DomainEvent event )
        {
            for (EventListener listener : listeners)
            {
                listener.notifyEvent(event);
            }
        }

        public void replayEvents(Date afterDate)
        {
/*
            Iterable<TransactionEvents> transactions = eventStore.events( afterDate, 100 );
            for (TransactionEvents transaction : transactions)
            {
                UnitOfWork uow = uowf.newUnitOfWork();
                for (DomainEvent domainEvent : transaction.events().get())
                {
                    // Get the entity
                    Class entityType = module.classLoader().loadClass(domainEvent.entityType().get() );
                    String id = domainEvent.entity().get();
                    Object entity = uow.get(entityType, id);

                    // Get method
                    Method eventMethod = getEventMethod(entity.getClass(), domainEvent.name().get());

                    if (eventMethod == null)
                    {
                        Logger.getLogger( Events.class.getName() ).warning( "Could not find event method "+domainEvent.name().get()+" in entity of type "+entityType.getName() );
                        continue;
                    }

                    // Build parameters
                    String jsonParameters = domainEvent.parameters().get();
                    org.json.JSONObject parameters = new JSONTokener(jsonParameters).nextValue();
                    for (int i = 0; i < eventMethod.getParameterTypes().length; i++)
                    {
                        Class<?> parameterType = eventMethod.getParameterTypes()[i];

                    }
                    Iterator keys = parameters.keys();
                    while (keys.hasNext())
                    {
                        String key = (String) parameters.keys().next();


                    }
                }
                uow.complete();
            }
*/
        }

        private Method getEventMethod( Class<? extends Object> aClass, String eventName )
        {
            for (Method method : aClass.getMethods())
            {
                if (method.getName().equals(eventName))
                    return method;
            }
            return null;
        }
    }
}
