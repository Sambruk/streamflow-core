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

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.ModuleSPI;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * JMX Management for StreamFlow. Exposes all configurable services as MBeans,
 * as well as the default ManagerComposite
 */
@Mixins(ManagerService.Mixin.class)
public interface ManagerService
        extends ServiceComposite, Activatable
{
    class Mixin
            implements Activatable
    {
        @Service
        MBeanServer server;

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        Qi4jSPI spi;

        @Uses
        TransientBuilder<ManagerComposite> managerBuilder;

        @Uses
        ObjectBuilder<CompositeMBean> mbeanBuilder;

        @Service
        Iterable<ServiceReference<Configuration>> configurableServices;

        public ObjectName objectName;
        private List<ObjectName> configurableServiceNames = new ArrayList<ObjectName>();
        public ManagerComposite manager;

        public void activate() throws Exception
        {
            ResourceBundle bundle = ResourceBundle.getBundle(Manager.class.getName());

            Properties version = new Properties();
            version.load(getClass().getResourceAsStream("/version.properties"));

            String versionString = version.getProperty("application.name")+" "+
                    version.getProperty("application.version")+" build:"+
                    version.getProperty("application.buildNumber")+" revision:"+
                    version.getProperty("application.revision");
            managerBuilder.prototype().version().set(versionString);

            manager = managerBuilder.newInstance();
            CompositeMBean mbean = mbeanBuilder.use(manager, Manager.class, bundle).newInstance();

            manager.activate();

            // Register the Model MBean in the MBean Server
            objectName = new ObjectName("StreamFlow:name=Manager");
            server.registerMBean(mbean, objectName);

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
                ModuleSPI module = (ModuleSPI) spi.getModule(configurableService);
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
                ObjectName configurableServiceName = new ObjectName("StreamFlow:name=" + name);
                server.registerMBean(mbean, configurableServiceName);
                configurableServiceNames.add(configurableServiceName);
            }


        }

        public void passivate() throws Exception
        {
            manager.passivate();

            server.unregisterMBean(objectName);
            for (ObjectName configurableServiceName : configurableServiceNames)
            {
                server.unregisterMBean(configurableServiceName);
            }
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
