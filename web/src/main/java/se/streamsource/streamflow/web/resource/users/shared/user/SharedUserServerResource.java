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

package se.streamsource.streamflow.web.resource.users.shared.user;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.web.resource.BaseServerResource;

/**
 * Mapped to:
 * /users/{id}/shared/user/*
 */
public class SharedUserServerResource
        extends BaseServerResource
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    Module module;

    @Structure
    Qi4jSPI spi;

    @Override
    protected Representation get() throws ResourceException
    {
        return getHtml("resources/shareduser.html");
    }


/*
    @Override
    protected Representation get() throws ResourceException
    {
        Map<String,Object> attributes = getRequest().getAttributes();
        String userId = attributes.get("id").toString();

        UnitOfWork uow = uowf.newUnitOfWork();

        UserEntity users = uow.get(UserEntity.class, userId);

        try
        {
            String role1 = (String) attributes.get("role1");

            if (role1 == null)
            {
                String template = TemplateUtil.getTemplate("resources/shareduser.html", getClass());
                String links = TemplateUtil.methodList(SharedUser.class);
                template = TemplateUtil.eval(template, "$content", links);
                return new StringRepresentation(template, MediaType.TEXT_HTML);
            } else
            {
                Method method = users.getClass().getMethod(role1);
                Object result = method.invoke(users);

                String role2 = (String) attributes.get("role2");

                if (role2 == null)
                {
                    String template = TemplateUtil.getTemplate("resources/links.html", getClass());
                    String links = TemplateUtil.methodList(Inbox.class);
                    template = TemplateUtil.eval(template, "$content", links, "$title", role1);
                    return new StringRepresentation(template, MediaType.TEXT_HTML);
                } else
                {
                    Method roleMethod = getMethod(result.getClass(), role2);

                    Object parameter = null;

                    if (roleMethod.getParameterTypes().length == 1)
                    {
                        if (roleMethod.getReturnType().equals(Void.class))
                        {
                            // Command
                            if (!getRequest().isEntityAvailable())
                            {
                                // Needs command
                                ValueComposite value = (ValueComposite) vbf.newValue(roleMethod.getParameterTypes()[0]);
                                String json = value.toJSON();
                                return new StringRepresentation(json);
                            } else
                            {
                                String json = getRequest().getEntityAsText();
                                parameter = vbf.newValueFromJSON(roleMethod.getParameterTypes()[0], json);
                            }
                        } else
                        {
                            if (!getRequest().getResourceRef().hasQuery())
                            {
                                // Needs command/query
                                ValueComposite value = (ValueComposite) vbf.newValue(roleMethod.getParameterTypes()[0]);

                                final StringBuilder form = new StringBuilder();
                                StateHolder holder = spi.getState(value);
                                final ValueDescriptor descriptor = spi.getValueDescriptor(value);
                                holder.visitProperties(new StateHolder.StateVisitor()
                                {
                                    public void visitProperty(QualifiedName name, Object value)
                                    {
                                        if (value != null)
                                        {
                                            PropertyTypeDescriptor propertyDesc = descriptor.state().getPropertyByQualifiedName(name);
                                            String queryParam = propertyDesc.propertyType().type().toQueryParameter(value);
                                            String propName = propertyDesc.qualifiedName().name();
                                            form.append(propName +":<input type=\"text\" name=\""+propName+"\" value=\""+queryParam+"\"/><br/>\n");
                                        }
                                    }
                                });

                                String formString = form.toString();

                                String template = TemplateUtil.getTemplate("resources/query.html", getClass());
                                template = TemplateUtil.eval(template, "$content", formString, "$title", role2);


//                                String json = value.toJSON();
                                return new StringRepresentation(template, MediaType.TEXT_HTML);
                            } else
                            {
                                final Form asForm = getRequest().getResourceRef().getQueryAsForm();
                                Class<?> valueType = roleMethod.getParameterTypes()[0];
                                ValueBuilder builder = vbf.newValueBuilder(valueType);
                                final ValueDescriptor descriptor = spi.getValueDescriptor((ValueComposite) builder.prototype());
                                builder.withState(new StateHolder()
                                {
                                    public <T> Property<T> getProperty(Method propertyMethod)
                                    {
                                        return null;
                                    }

                                    public void visitProperties(StateVisitor visitor)
                                    {
                                        for (PropertyType propertyType : descriptor.valueType().types())
                                        {
                                            String value = asForm.getFirstValue(propertyType.qualifiedName().name());
                                            Object valueObject = propertyType.type().fromQueryParameter(value, module);
                                            visitor.visitProperty(propertyType.qualifiedName(), valueObject);
                                        }
                                    }
                                });

                                parameter = builder.newInstance();
                            }
                        }
                    }

                    Object result2;
                    roleMethod.setAccessible(true);
                    if (parameter != null)
                        result2 = roleMethod.invoke(result, parameter);
                    else
                        result2 = roleMethod.invoke(result);

                    // Serialize value
                    if (Iterable.class.isAssignableFrom(roleMethod.getReturnType()))
                    {
                        final Iterable iterable = (Iterable) result2;
                        return new WriterRepresentation(MediaType.APPLICATION_JSON)
                        {
                            public void write(Writer writer) throws IOException
                            {
                                writer.append('[');
                                Iterator iter = iterable.iterator();
                                while (iter.hasNext())
                                {
                                    Object value = iter.next();

                                    String json = ((ValueComposite)value).toJSON();
                                    writer.write(json);

                                    if (iter.hasNext())
                                        writer.append(',');

                                }
                                writer.append(']');
                            }
                        };
                    } else
                    {
                        throw new ResourceException(TaskStates.SERVER_ERROR_INTERNAL, "Unknown representation type:"+result2.getClass().getName());
                    }
                }
            }
        } catch (NoSuchMethodException e)
        {
            throw new ResourceException(TaskStates.CLIENT_ERROR_NOT_FOUND);
        } catch (IllegalAccessException e)
        {
            throw new ResourceException(TaskStates.SERVER_ERROR_INTERNAL, e);
        } catch (InvocationTargetException e)
        {
            throw new ResourceException(TaskStates.SERVER_ERROR_INTERNAL, e.getTargetException());
        } catch (IOException e)
        {
            throw new ResourceException(TaskStates.SERVER_ERROR_INTERNAL, e);
        } finally
        {
            uow.discard();
        }
    }

    private Method getMethod(Class clazz, String methodName) throws ResourceException
    {
        for (Method method : clazz.getMethods())
        {
            if (method.getName().equals(methodName))
                return method;
        }

        throw new ResourceException(TaskStates.CLIENT_ERROR_NOT_FOUND);
    }
*/
}
