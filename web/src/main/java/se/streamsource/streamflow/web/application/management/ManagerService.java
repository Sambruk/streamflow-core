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

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.property.Property;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.structure.Module;
import org.qi4j.entitystore.jdbm.DatabaseExport;
import org.qi4j.entitystore.jdbm.DatabaseImport;
import org.qi4j.index.reindexer.Reindexer;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.entity.EntityDescriptor;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.AllEventsSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.infrastructure.database.MySQLDatabaseConfiguration;

import javax.management.*;
import javax.management.modelmbean.*;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * JMX Management MBean for StreamFlow
 */
@Mixins(ManagerService.ManagerMixin.class)
public interface ManagerService
        extends ManagerOperations, ServiceComposite, Activatable
{
    class ManagerMixin
            implements ManagerOperations, ManagerAttributes, Activatable
    {
        @Service
        MBeanServer server;

        @Service
        Reindexer reindexer;

        @Service
        DatabaseExport exportDatabase;

        @Service
        DatabaseImport importDatabase;

        @Service
        EventStore eventStore;

        @Service
        FileConfiguration fileConfig;

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        Qi4jSPI spi;

        @Service
        Iterable<ServiceReference<Configuration>> configurableServices;

        public File exports;

        public void activate() throws Exception
        {
            exports = new File(fileConfig.dataDirectory(), "exports");
            exports.mkdirs();

            // Register methods as operations
            Method[] methods = ManagerOperations.class.getMethods();
            ResourceBundle bundle = ResourceBundle.getBundle(ManagerOperations.class.getName());
            ModelMBeanOperationInfo[] operations = new ModelMBeanOperationInfo[methods.length];
            for (int i = 0; i < methods.length; i++)
            {
                Method method = methods[i];
                String name = method.getName();
                try
                {
                    name = bundle.getString(name + ".name");
                } catch (MissingResourceException e)
                {
                    // Ignore
                }

                MBeanParameterInfo[] signature = new MBeanParameterInfo[method.getParameterTypes().length];
                Annotation[][] annotations = method.getParameterAnnotations();
                for (int j = 0; j < method.getParameterTypes().length; j++)
                {
                    Class<?> parameterType = method.getParameterTypes()[j];
                    Name paramName = getAnnotationOfType(annotations[j], Name.class);
                    String nameStr = paramName == null ? "param" + j : paramName.value();
                    signature[j] = new MBeanParameterInfo(nameStr, parameterType.getName(), nameStr);
                }

                ModelMBeanOperationInfo operation =
                        new ModelMBeanOperationInfo(method.getName(), name, signature, method.getReturnType().getName(), MBeanOperationInfo.ACTION);
                operations[i] = operation;
            }

            // Register methods as attributes
            methods = ManagerAttributes.class.getMethods();
            ModelMBeanAttributeInfo[] attributes = new ModelMBeanAttributeInfo[methods.length/2];
            int idx = 0;
            for (Method method : methods)
            {
                if (method.getName().startsWith("get"))
                {
                    String name = method.getName().substring(3);
                    ModelMBeanAttributeInfo attribute = new ModelMBeanAttributeInfo(name, name, method, ManagerAttributes.class.getMethod("set"+name, method.getReturnType()));
                    attributes[idx++] = attribute;
                }
            }


            ModelMBeanInfo mmbi =
                    new ModelMBeanInfoSupport(ManagerOperations.class.getName(),
                            "StreamFlow manager",
                            attributes,
                            null,  // no constructors
                            operations,
                            null); // no notifications

            // Make the Model MBean and link it to the resource
            ModelMBean mmb = new RequiredModelMBean(mmbi);
            mmb.setManagedResource(this, "ObjectReference");

            // Register the Model MBean in the MBean Server
            ObjectName mapName = new ObjectName("StreamFlow:name=Manager");
            server.registerMBean(mmb, mapName);

            // Expose configurable services
            exportConfigurableServices();
        }

        private void exportConfigurableServices() throws NotCompliantMBeanException, MBeanRegistrationException, InstanceAlreadyExistsException, MalformedObjectNameException
        {
            for (ServiceReference<Configuration> configurableService : configurableServices)
            {
                String serviceClass = configurableService.get().getClass().getInterfaces()[0].getName();
                String name = configurableService.identity();
                ServiceDescriptor serviceDescriptor = spi.getServiceDescriptor(configurableService);
                ModuleSPI module = (ModuleSPI) configurableService.module();
                EntityDescriptor descriptor = module.entityDescriptor(serviceDescriptor.configurationType().getName());
                List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
                Map<String, QualifiedName> properties = new HashMap<String, QualifiedName>();
                for (PropertyType propertyType : descriptor.entityType().properties())
                {
                    if (propertyType.propertyType() == PropertyType.PropertyTypeEnum.MUTABLE)
                    {
                        String propertyName = propertyType.qualifiedName().name();
                        String type = propertyType.type().type().name();
                        attributes.add(new MBeanAttributeInfo(propertyName, type, propertyName, true, true, type.equals("java.lang.Boolean")));
                        properties.put(propertyName, propertyType.qualifiedName());
                    }
                }

                List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();
                if (configurableService instanceof Activatable)
                {
                    operations.add(new MBeanOperationInfo("restart", "Restart service", new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION_INFO));
                }

                MBeanInfo mbeanInfo = new MBeanInfo(serviceClass, name, attributes.toArray(new MBeanAttributeInfo[attributes.size()]), null, operations.toArray(new MBeanOperationInfo[operations.size()]), null);
                Object mbean = new ConfigurableService(configurableService, mbeanInfo, name, properties);
                server.registerMBean(mbean, new ObjectName("StreamFlow:name="+name));
            }
        }

        public void passivate() throws Exception
        {
        }

        // Operations
        public void reindex()
        {
            reindexer.reindex();
        }

        public String exportDatabase(boolean compress) throws IOException
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmm");
            File exportFile = new File(exports, "streamflow_data_" + format.format(new Date()) + (compress ? ".json.gz" : ".json"));
            OutputStream out = new FileOutputStream(exportFile);

            if (compress)
            {
                out = new GZIPOutputStream(out);
            }

            Writer writer = new OutputStreamWriter(out, "UTF-8");
            exportDatabase.exportTo(writer);
            writer.close();

            return "Database exported to " + exportFile.getAbsolutePath();
        }

        public String importDatabase(@Name("Filename") String name) throws IOException
        {
            File importFile = new File(exports, name);

            if (!importFile.exists())
                return "No such import file:" + importFile.getAbsolutePath();

            InputStream in1 = new FileInputStream(importFile);
            if (importFile.getName().endsWith("gz")) ;
            in1 = new GZIPInputStream(in1);
            Reader in = new InputStreamReader(in1, "UTF-8");

            try
            {
                importDatabase.importFrom(in);
            } finally
            {
                in.close();
            }

            return "Data imported successfully";
        }

        public String exportEvents(@Name("Compress") boolean compress) throws IOException
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmm");
            File exportFile = new File(exports, "streamflow_events_" + format.format(new Date()) + (compress ? ".json.gz" : ".json"));
            OutputStream out = new FileOutputStream(exportFile);

            if (compress)
            {
                out = new GZIPOutputStream(out);
            }

            Writer writer = new OutputStreamWriter(out, "UTF-8");
            Iterable<DomainEvent> events = eventStore.events(new AllEventsSpecification(), null, Integer.MAX_VALUE);
            for (DomainEvent event : events)
            {
                writer.write(event.toJSON()+"\n");
            }

            writer.close();

            return "Events exported to " + exportFile.getAbsolutePath();
        }

        // Attributes
        private <T extends Annotation> T getAnnotationOfType(Annotation[] annotations, Class<T> annotationType)
        {
            for (Annotation annotation : annotations)
            {
                if (annotationType.equals(annotation.annotationType()))
                {
                    return annotationType.cast(annotation);
                }
            }
            return null;
        }

        class ConfigurableService
            implements DynamicMBean
        {
            ServiceReference<Configuration> service;
            MBeanInfo info;
            String identity;
            Map<String, QualifiedName> propertyNames;

            ConfigurableService(ServiceReference<Configuration> service, MBeanInfo info, String identity, Map<String, QualifiedName> propertyNames)
            {
                this.service = service;
                this.info = info;
                this.identity = identity;
                this.propertyNames = propertyNames;
            }

            public Object getAttribute(String name) throws AttributeNotFoundException, MBeanException, ReflectionException
            {
                UnitOfWork uow = uowf.newUnitOfWork();
                try
                {
                    Entity configuration = uow.get(Entity.class, identity);
                    EntityStateHolder state = spi.getState((EntityComposite) configuration);
                    QualifiedName qualifiedName = propertyNames.get(name);
                    Property<Object> property = state.getProperty(qualifiedName);
                    return property.get();
                } catch (Exception ex)
                {
                    throw new ReflectionException(ex, "Could not get attribute "+name);
                } finally
                {
                    uow.discard();
                }
            }

            public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
            {
                UnitOfWork uow = uowf.newUnitOfWork();
                try
                {
                    Entity configuration = uow.get(Entity.class, identity);
                    EntityStateHolder state = spi.getState((EntityComposite) configuration);
                    QualifiedName qualifiedName = propertyNames.get(attribute.getName());
                    Property<Object> property = state.getProperty(qualifiedName);
                    property.set(attribute.getValue());
                    uow.complete();
                } catch (Exception ex)
                {
                    uow.discard();
                }
            }

            public AttributeList getAttributes(String[] names)
            {
                AttributeList list = new AttributeList();
                for (String name : names)
                {
                    try
                    {
                        Object value = getAttribute(name);
                        list.add(new Attribute(name, value));
                    } catch (AttributeNotFoundException e)
                    {
                        e.printStackTrace();
                    } catch (MBeanException e)
                    {
                        e.printStackTrace();
                    } catch (ReflectionException e)
                    {
                        e.printStackTrace();
                    }
                }

                return list;
            }

            public AttributeList setAttributes(AttributeList attributeList)
            {
                AttributeList list = new AttributeList();
                for (int i = 0; i < list.size(); i++)
                {
                    Attribute attribute = (Attribute) list.get(i);

                    try
                    {
                        setAttribute(attribute);
                        list.add(attribute);
                    } catch (AttributeNotFoundException e)
                    {
                        e.printStackTrace();
                    } catch (InvalidAttributeValueException e)
                    {
                        e.printStackTrace();
                    } catch (MBeanException e)
                    {
                        e.printStackTrace();
                    } catch (ReflectionException e)
                    {
                        e.printStackTrace();
                    }
                }

                return list;
            }

            public Object invoke(String s, Object[] objects, String[] strings) throws MBeanException, ReflectionException
            {
                if (s.equals("restart"))
                {
                    try
                    {
                        // Refresh and restart
                        if (service.isActive())
                        {
                            // Refresh configuration
                            service.get().refresh();

                            ((Activatable)service).passivate();
                            ((Activatable)service).activate();
                        }

                        return "Service restarted";
                    } catch (Exception e)
                    {
                        return "Could not restart service:"+e.getMessage();
                    }
                }

                return "Unknown operation";
            }

            public MBeanInfo getMBeanInfo()
            {
                return info;
            }
        }
    }
}
