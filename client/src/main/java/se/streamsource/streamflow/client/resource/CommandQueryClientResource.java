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

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ConnectionException;

import java.io.IOException;

/**
 * Base class for client-side Command/Query resources
 */
public class CommandQueryClientResource
        extends BaseClientResource
{
    @Structure
    protected UnitOfWorkFactory uowf;

    @Structure
    protected ValueBuilderFactory vbf;

    @Structure
    protected Qi4jSPI spi;

    @Structure
    protected Module module;

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
                throw new ConnectionException(e.getMessage(), e);
            }
        } else if (getResponse().getStatus().equals(Status.SERVER_ERROR_INTERNAL))
        {
            if (getResponse().getEntity().getMediaType().equals(MediaType.APPLICATION_JAVA_OBJECT))
            {
                try
                {
                    Object exception = new ObjectRepresentation(result).getObject();
                    throw new ConnectionException("Could not process query", (Throwable) exception);
                } catch (IOException e)
                {
                    throw new ConnectionException(e.getMessage());
                } catch (ClassNotFoundException e)
                {
                    throw new ConnectionException(e.getMessage());
                }
            }

            throw new ConnectionException(getResponse().getEntityAsText());
        } else
        {
            try
            {
                String text = getResponseEntity().getText();
                throw new ConnectionException(text);
            } catch (IOException e)
            {
                throw new ConnectionException("Could not get response text");
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
            post(commandRepresentation);
        } finally
        {
            setReference(ref);
        }
    }

    protected void putCommand(String operation, ValueComposite command) throws ResourceException
    {

        StringRepresentation json = new StringRepresentation(command.toJSON(), MediaType.APPLICATION_JSON);

        Reference ref = getReference();
        Reference operationRef = ref.clone().addSegment(operation);
        setReference(operationRef);
        try
        {
            int tries = 3;
            while (true)
            {
                try
                {
                    put(json);
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
}
