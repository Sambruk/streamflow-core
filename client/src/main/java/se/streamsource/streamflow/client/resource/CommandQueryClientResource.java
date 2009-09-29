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

package se.streamsource.streamflow.client.resource;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

/**
 * Base class for client-side Command/Query resources
 */
public class CommandQueryClientResource
        extends BaseClientResource
{
    @Structure
    protected UnitOfWorkFactory uowf;

    @Structure
    protected Qi4jSPI spi;

    @Structure
    protected Module module;

    @Service
    protected EventSourceListener eventListener;

    public CommandQueryClientResource(@Uses org.restlet.Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    protected <T extends ValueComposite> T query(String operation, Class<T> queryResult) throws ResourceException
    {
        return query(operation, null, queryResult);
    }

    protected <T extends ValueComposite> T query(String operation, ValueComposite queryValue, Class<T> queryResult) throws ResourceException
    {
        Reference ref = getReference();
        Reference operationRef = ref.clone();
        if (queryValue != null)
            setQueryParameters(operationRef, queryValue);
        operationRef.addQueryParameter("operation", operation);

        operationRef = new Reference(operationRef.toUrl());
        setReference(operationRef);
        Representation result;
        try
        {
            System.out.println(operationRef.toUrl());
            result = get(MediaType.APPLICATION_JSON);
        } finally
        {
            setReference(ref);
        }

        if (getResponse().getStatus().isSuccess())
        {
            try
            {
                String jsonValue = result.getText();
                T returnValue = vbf.newValueFromJSON(queryResult, jsonValue);
                return returnValue;
            } catch (IOException e)
            {
                throw new ResourceException(e);
            }
        } else if (getResponse().getStatus().equals(Status.SERVER_ERROR_INTERNAL))
        {
            if (getResponse().getEntity().getMediaType().equals(MediaType.APPLICATION_JAVA_OBJECT))
            {
                try
                {
                    Object exception = new ObjectRepresentation(result).getObject();
                    throw new ResourceException((Throwable) exception);
                } catch (IOException e)
                {
                    throw new ResourceException(e);
                } catch (ClassNotFoundException e)
                {
                    throw new ResourceException(e);
                }
            }

            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, getResponse().getEntityAsText());
        } else
        {
            try
            {
                if (getResponseEntity() != null)
                {
                    String text = getResponseEntity().getText();
                    throw new ResourceException(getResponse().getStatus(), text);
                } else
                {
                    throw new ResourceException(getResponse().getStatus());
                }
            } catch (IOException e)
            {
                throw new ResourceException(e);
            }
        }
    }

    private void setQueryParameters(final Reference ref, ValueComposite queryValue)
    {
        // Value as parameter
        StateHolder holder = spi.getState(queryValue);
        final ValueDescriptor descriptor = spi.getValueDescriptor(queryValue);

        ref.setQuery(null);

        holder.visitProperties(new StateHolder.StateVisitor()
        {
            public void visitProperty(QualifiedName
                    name, Object value)
            {
                if (value != null)
                {
                    PropertyTypeDescriptor propertyDesc = descriptor.state().getPropertyByQualifiedName(name);
                    String queryParam = propertyDesc.propertyType().type().toQueryParameter(value);
                    ref.addQueryParameter(name.name(), queryParam);
                }
            }
        });
    }

    protected void postCommand(String operation) throws ResourceException
    {
        postCommand(operation, null);
    }

    protected void postCommand(String operation, ValueComposite command) throws ResourceException
    {
        Representation commandRepresentation;
        if (command != null)
            commandRepresentation = new StringRepresentation(command.toJSON(), MediaType.APPLICATION_JSON);
        else
            commandRepresentation = new EmptyRepresentation();

        Reference ref = getReference();
        Reference operationRef = ref.clone().addQueryParameter("operation", operation);
        setReference(operationRef);
        try
        {
            Representation events = post(commandRepresentation);
            if (!getStatus().isSuccess())
            {
                throw new ResourceException(getStatus());
            } else
            {
                processEvents(events);
            }
        } finally
        {
            setReference(ref);
        }
    }

    private void processEvents(Representation entity)
    {
        if (getResponse().getStatus().isSuccess() && (getRequest().getMethod().equals(Method.POST) || getRequest().getMethod().equals(Method.DELETE) || getRequest().getMethod().equals(Method.PUT)))
        {
            try
            {
                if (entity != null && !(entity instanceof EmptyRepresentation))
                {
                    String source = entity.getText();

                    final TransactionEvents transactionEvents = vbf.newValueFromJSON(TransactionEvents.class,  source);

                    eventListener.eventsAvailable(new EventStore()
                    {
                        public Iterable<TransactionEvents> events(@Optional Date afterTimestamp, int maxTransactions)
                        {
                            return Collections.singletonList(transactionEvents);
                        }
                    });
                }
            } catch (Exception e)
            {
                throw new OperationException(StreamFlowResources.could_not_process_events, e);
            }
        }
    }

    public void create() throws ResourceException
    {
        putCommand(null);
    }

    public void putCommand(String operation) throws ResourceException
    {
        putCommand(operation, null);
    }

    protected void putCommand(String operation, ValueComposite command) throws ResourceException
    {
        Representation commandRepresentation;
        if (command != null)
            commandRepresentation = new StringRepresentation(command.toJSON(), MediaType.APPLICATION_JSON);
        else
            commandRepresentation = new EmptyRepresentation();

        Reference ref = getReference();
        if (operation != null)
        {
            Reference operationRef = ref.clone().addQueryParameter("operation", operation);
            setReference(operationRef);
        }
        try
        {
            int tries = 3;
            while (true)
            {
                try
                {
                    Representation events = put(commandRepresentation);
                    if (!getStatus().isSuccess())
                    {
                        throw new ResourceException(getStatus());
                    } else
                    {
                        processEvents(events);
                    }
                    break;
                } catch (ResourceException e)
                {
                    if (e.getStatus().equals(Status.CONNECTOR_ERROR_COMMUNICATION) ||
                            e.getStatus().equals(Status.CONNECTOR_ERROR_CONNECTION))
                    {
                        if (tries == 0)
                            throw e; // Give up
                        else
                        {
                            // Try again
                            tries--;
                            continue;
                        }
                    } else
                    {
                        // Abort
                        throw e;
                    }
                }
            }
        } finally
        {
            setReference(ref);
        }
    }

    public void deleteCommand() throws ResourceException
    {

        int tries = 3;
        while (true)
        {
            try
            {
                Representation events = delete();
                if (!getStatus().isSuccess())
                {
                    throw new ResourceException(getStatus());
                } else
                {
                    processEvents(events);
                }
                break;
            } catch (ResourceException e)
            {
                if (e.getStatus().equals(Status.CONNECTOR_ERROR_COMMUNICATION) ||
                        e.getStatus().equals(Status.CONNECTOR_ERROR_CONNECTION))
                {
                    if (tries == 0)
                        throw e; // Give up
                    else
                    {
                        // Try again
                        tries--;
                        continue;
                    }
                } else
                {
                    // Abort
                    throw e;
                }
            }
        }
    }
}
