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

package se.streamsource.streamflow.web.resource;

import org.json.JSONException;
import org.json.JSONWriter;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.util.Annotations;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.representation.*;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.*;
import se.streamsource.streamflow.web.infrastructure.web.TemplateUtil;

import javax.security.auth.Subject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for command/query resources.
 * <p/>
 * GET: if has ?operation=name then show XHTML form
 * for invoking operation with name "name". Otherwise
 * return whatever makes sense (listing, query form(s), etc.)
 * <p/>
 * POST: post of form must include names "operation" and "command",
 * where "operation" is the name of the method to invoke and "command
 * is the JSON-serialized command value.
 * <p/>
 * PUT: put of form must include names "operation" and "command",
 * where "operation" is the name of the method to invoke and "command
 * is the JSON-serialized command value. Must be an idempotent operation.
 * <p/>
 * DELETE: resources implement this on their own.
 */
public class CommandQueryServerResource
        extends BaseServerResource
        implements EventSourceListener
{
    @Structure
    protected ValueBuilderFactory vbf;

    @Structure
    protected ModuleSPI module;

    @Service
    EventSource source;
    public Iterable<DomainEvent> events;

    public CommandQueryServerResource()
    {
        getVariants().put(org.restlet.data.Method.ALL, Arrays.asList(MediaType.TEXT_HTML, MediaType.APPLICATION_JSON));

        setNegotiated(true);
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        String operation = getOperation();
        if (operation == null)
        {
            return listOperations();
        } else
        {
            UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase(operation));

            try
            {
                Method method = getResourceMethod(operation);

                if (getRequest().getResourceRef().hasQuery() || method.getParameterTypes().length == 0)
                {
                    // Invoke query
                    Object[] args = getQueryArguments(method);
                    return returnRepresentation(invoke(method, args), variant);
                } else
                {
                    // Show form
                    return new StringRepresentation(""); // TODO
                }
            } finally
            {
                uow.discard();
            }
        }
    }

    protected String getOperation()
    {
        String operation = getRequest().getResourceRef().getQueryAsForm().getFirstValue("operation");
        if (operation == null)
        {
            operation = getRequest().getMethod().getName().toLowerCase() + "Operation";
        }
        return operation;
    }

    protected Representation listOperations() throws ResourceException
    {
		// List methods
		StringBuilder links = new StringBuilder("");
		Method[] methods = getClass().getMethods();
		for (Method method : methods)
		{
			if (isQueryMethod(method))
				links.append("<li><a href=\"?operation=").append(
						method.getName()).append("\" rel=\"").append(
						method.getName()).append("\">")
						.append(method.getName()).append("</a></li>\n");
		}

		try
		{
			String template = TemplateUtil.getTemplate("resources/links.html",
					CommandQueryServerResource.class);
			String content = TemplateUtil.eval(template, "$content", links.toString(),
					"$title", getRequest().getResourceRef().getLastSegment()
							+ " operations");
			return new StringRepresentation(content, MediaType.TEXT_HTML);
		} catch (IOException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
    }

    @Override
    final protected Representation delete(Variant variant) throws ResourceException
    {
        return post(null, variant);
    }

    @Override
    final protected Representation post(Representation entity, Variant variant) throws ResourceException
    {
        String operation = getOperation();
        UnitOfWork uow = null;
        source.registerListener(this, AllEventsSpecification.INSTANCE, false);
        try
        {
            Method method = getResourceMethod(operation);
            Object[] args = getCommandArguments(method);

            int tries = 0;
            while (tries < 10)
            {
                uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase(operation));

                Representation rep = returnRepresentation(invoke(method, args), variant);
                try
                {
                    uow.complete();

                    if (rep instanceof EmptyRepresentation && events != null)
                    {
                        rep = new WriterRepresentation(MediaType.APPLICATION_JSON)
                        {
                            public void write(Writer writer) throws IOException
                            {
                                int count = 0;
                                ValueType type = module.valueDescriptor(DomainEvent.class.getName()).valueType();
                                try
                                {
                                    JSONWriter json = new JSONWriter(writer).array();
                                    for (DomainEvent event : events)
                                    {
                                        type.toJSON(event, json);
                                        count++;
                                    }
                                    json.endArray();
                                } catch (JSONException e)
                                {
                                    throw (IOException) new IOException("Could not write JSON").initCause(e);
                                }
                            }
                        };
                    }

                    return rep;
                } catch (ConcurrentEntityModificationException e)
                {
                    // Try again
                } catch (Exception ex)
                {
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex);
                }
            }

            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not invoke command");

        } catch (ResourceException ex)
        {
            if (uow != null)
                uow.discard();

            throw ex;
        } catch (Exception ex)
        {
            Logger.getLogger("command").log(Level.SEVERE, "Could not process command:"+operation, ex);

            setStatus(Status.SERVER_ERROR_INTERNAL);
            return new ObjectRepresentation(ex, MediaType.APPLICATION_JAVA_OBJECT);
        } finally
        {
            source.unregisterListener(this);
        }
    }

    private Representation returnRepresentation(Object returnValue, Variant variant) throws ResourceException
    {
        if (returnValue != null)
        {
            if (returnValue instanceof ValueComposite)
            {
                if (variant.getMediaType().equals(MediaType.APPLICATION_JSON))
                {
                    return new StringRepresentation(((Value) returnValue).toJSON(), MediaType.APPLICATION_JSON);
                } else if (variant.getMediaType().equals(MediaType.TEXT_HTML))
                {
                    try
                    {
                        String template = TemplateUtil.getTemplate("resources/value.html", CommandQueryServerResource.class);
                        String content = TemplateUtil.eval(template, "$content", ((Value) returnValue).toJSON());
                        return new StringRepresentation(content, MediaType.TEXT_HTML);
                    } catch (IOException e)
                    {
                        throw new ResourceException(e);
                    }
                } else
                {
                    return new EmptyRepresentation();
                }
            } else
            {
                Logger.getLogger(getClass().getName()).warning("Unknown result type:" + returnValue.getClass().getName());
                return new EmptyRepresentation();
            }
        } else
            return new EmptyRepresentation();
    }

    @Override
    final protected Representation put(Representation representation, Variant variant) throws ResourceException
    {
        return post(representation, variant);
    }

    public void eventsAvailable(EventStore source, EventSpecification specification)
    {
        events = source.events(specification, null, Integer.MAX_VALUE);
    }

    private Method getResourceMethod(String operation)
            throws ResourceException
    {
        for (Method method : getClass().getMethods())
        {
            if (method.getName().equals(operation))
            {
                return method;
            }
        }
        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
    }

    private boolean isQueryMethod(Method method)
    {
        return Value.class.isAssignableFrom(method.getReturnType());
    }

    private Object[] getCommandArguments(Method method) throws ResourceException
    {
        if (method.getParameterTypes().length > 0)
        {
            Class<? extends Value> commandType = (Class<? extends Value>) method.getParameterTypes()[0];

            if (getRequest().getEntity().getMediaType().equals(MediaType.APPLICATION_JSON))
            {
                String json = getRequest().getEntityAsText();
                if (json == null)
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!");

                Object command = vbf.newValueFromJSON(commandType, json);

                return new Object[]{command};
            } else if (getRequest().getEntity().getMediaType().equals(MediaType.TEXT_PLAIN))
            {
                String text = getRequest().getEntityAsText();
                if (text == null)
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!");
                return new Object[]{text};
            } else
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Command has to be in JSON format");
        } else
        {
            return new Object[0];
        }
    }

    private Object[] getQueryArguments(Method method)
            throws ResourceException
    {
        Object[] args = new Object[method.getParameterTypes().length];
        int idx = 0;

        Representation representation = getRequest().getEntity();
        if (representation != null && MediaType.APPLICATION_JSON.equals(representation.getMediaType()))
        {
            Class<?> valueType = method.getParameterTypes()[0];
            String json = getRequest().getEntityAsText();
            if (json == null)
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Bug in Tomcat encountered; notify developers!");
            Object requestValue = vbf.newValueFromJSON(valueType, json);
            args[0] = requestValue;
        } else
        {
            final Form asForm = getRequest().getResourceRef().getQueryAsForm();

            if (args.length == 1 && ValueComposite.class.isAssignableFrom(method.getParameterTypes()[0]))
            {
                Class<?> valueType = method.getParameterTypes()[0];
                args[0] = getValueFromQuery((Class<ValueComposite>) valueType);
            } else
            {
                for (Annotation[] annotations : method.getParameterAnnotations())
                {
                    Name name = Annotations.getAnnotationOfType(annotations, Name.class);
                    Object arg = asForm.getFirstValue(name.value());

                    // Parameter conversion
                    if (method.getParameterTypes()[idx].equals(EntityReference.class))
                    {
                        arg = EntityReference.parseEntityReference(arg.toString());
                    }

                    args[idx++] = arg;
                }
            }
        }

        return args;
    }

    private Object invoke(final Method method, final Object[] args)
            throws ResourceException
    {
        try
        {
            Subject subject = getRequest().getClientInfo().getSubject();
            final Object commandObject = this;
            try
            {
                Object returnValue = Subject.doAs(subject, new PrivilegedExceptionAction()
                {
                    public Object run() throws Exception
                    {
                        Logger.getLogger("command").log(Level.SEVERE, "Could not invoke command:"+method.getName(), new Throwable());

                        return method.invoke(commandObject, args);
                    }
                });

                return returnValue;
            } catch (PrivilegedActionException e)
            {
                throw e.getCause();
            }
        } catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof ResourceException)
            {
                throw (ResourceException) e.getTargetException();
            }
            else if (e.getTargetException() instanceof AccessControlException)
            {
                // Operation not allowed - return 403
                throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
            }

            Logger.getLogger("command").log(Level.SEVERE, "Could not invoke command:"+method.getName(), e.getTargetException());

            getResponse().setEntity(new ObjectRepresentation(e));

            throw new ResourceException(e.getTargetException());
        } catch (Throwable e)
        {
            throw new ResourceException(e);
        }
    }

    private ValueComposite getValueFromQuery(Class<ValueComposite> valueType)
    {
        final Form asForm = getRequest().getResourceRef().getQueryAsForm();
        ValueBuilder<ValueComposite> builder = vbf.newValueBuilder(valueType);
        final ValueDescriptor descriptor = spi.getValueDescriptor(builder.prototype());
        builder.withState(new StateHolder()
        {
            public <T> Property<T> getProperty(QualifiedName name)
            {
                return null;
            }

            public <T> Property<T> getProperty(Method propertyMethod)
            {
                return null;
            }

            public void visitProperties(StateVisitor visitor)
            {
                for (PropertyType propertyType : descriptor.valueType().types())
                {
                    Parameter param = asForm.getFirst(propertyType.qualifiedName().name());
                    if (param != null)
                    {
                        String value = param.getValue();
                        if (value == null)
                            value = "";
                        try
                        {
                            Object valueObject = propertyType.type().fromQueryParameter(value, module);
                            visitor.visitProperty(propertyType.qualifiedName(), valueObject);
                        } catch (JSONException e)
                        {
                            throw new IllegalArgumentException("Query parameter has invalid JSON format", e);
                        }
                    }
                }
            }
        });
        return builder.newInstance();
    }
}
