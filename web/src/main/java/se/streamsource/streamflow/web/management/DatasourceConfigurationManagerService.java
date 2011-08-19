/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.management;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.Entity;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.EntityStateHolder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;
import se.streamsource.streamflow.web.infrastructure.database.DataSourceConfiguration;
import se.streamsource.streamflow.web.infrastructure.database.DataSourceService;

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
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Expose DatasourceConfiguration through JMX. Allow configurations to be edited, and the services to be restarted.
 */
@Mixins(DatasourceConfigurationManagerService.Mixin.class)
public interface DatasourceConfigurationManagerService
      extends ServiceComposite, Activatable
{
   class Mixin
         implements Activatable
   {
      @Structure
      Module module;

      @Service
      MBeanServer server;

      @Structure
      Qi4jSPI spi;

      @Structure
      Application application;

      @Service
      Iterable<ServiceReference<DataSource>> dataSources;
      @Service
      ServiceReference<DataSourceService> dataSourceService;

      private List<ObjectName> configurationNames = new ArrayList<ObjectName>();

      public void activate() throws Exception
      {
         // Expose configurable services
         exportDataSources();
      }

      private void exportDataSources() throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException
      {
         for (ServiceReference<DataSource> dataSource : dataSources)
         {
            String name = dataSource.identity();
            ModuleSPI module = (ModuleSPI) spi.getModule( dataSource );
            EntityDescriptor descriptor = module.entityDescriptor( DataSourceConfiguration.class.getName() );
            List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
            Map<String, QualifiedName> properties = new HashMap<String, QualifiedName>();
            for (PropertyType propertyType : descriptor.entityType().properties())
            {
               if (propertyType.propertyType() == PropertyType.PropertyTypeEnum.MUTABLE)
               {
                  String propertyName = propertyType.qualifiedName().name();
                  String type = propertyType.type().type().name();
                  attributes.add( new MBeanAttributeInfo( propertyName, type, propertyName, true, true, type.equals( "java.lang.Boolean" ) ) );
                  properties.put( propertyName, propertyType.qualifiedName() );
               }
            }

            List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();
            operations.add( new MBeanOperationInfo( "restart", "Restart DataSource", new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION_INFO ) );

            MBeanInfo mbeanInfo = new MBeanInfo( DataSourceConfiguration.class.getName(), name, attributes.toArray( new MBeanAttributeInfo[attributes.size()] ), null, operations.toArray( new MBeanOperationInfo[operations.size()] ), null );
            Object mbean = new ConfigurableDataSource( dataSourceService, mbeanInfo, name, properties );
            ObjectName configurableDataSourceName = new ObjectName( "Qi4j:application="+application.name()+",class=Datasource,name=" + name );
            server.registerMBean( mbean, configurableDataSourceName );
            configurationNames.add( configurableDataSourceName );
         }
      }

      public void passivate() throws Exception
      {
         for (ObjectName configurableServiceName : configurationNames)
         {
            server.unregisterMBean( configurableServiceName );
         }
      }

      abstract class EditableConfiguration
            implements DynamicMBean
      {
         MBeanInfo info;
         String identity;
         Map<String, QualifiedName> propertyNames;

         EditableConfiguration( MBeanInfo info, String identity, Map<String, QualifiedName> propertyNames )
         {
            this.info = info;
            this.identity = identity;
            this.propertyNames = propertyNames;
         }

         public Object getAttribute( String name ) throws AttributeNotFoundException, MBeanException, ReflectionException
         {
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();
            try
            {
               Entity configuration = uow.get( Entity.class, identity );
               EntityStateHolder state = spi.getState( (EntityComposite) configuration );
               QualifiedName qualifiedName = propertyNames.get( name );
               Property<Object> property = state.getProperty( qualifiedName );
               return property.get();
            } catch (Exception ex)
            {
               throw new ReflectionException( ex, "Could not get attribute " + name );
            } finally
            {
               uow.discard();
            }
         }

         public void setAttribute( Attribute attribute ) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
         {
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork();
            try
            {
               Entity configuration = uow.get( Entity.class, identity );
               EntityStateHolder state = spi.getState( (EntityComposite) configuration );
               QualifiedName qualifiedName = propertyNames.get( attribute.getName() );
               Property<Object> property = state.getProperty( qualifiedName );
               property.set( attribute.getValue() );
               uow.complete();
            } catch (Exception ex)
            {
               uow.discard();
            }
         }

         public AttributeList getAttributes( String[] names )
         {
            AttributeList list = new AttributeList();
            for (String name : names)
            {
               try
               {
                  Object value = getAttribute( name );
                  list.add( new Attribute( name, value ) );
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

         public AttributeList setAttributes( AttributeList attributeList )
         {
            AttributeList list = new AttributeList();
            for (int i = 0; i < list.size(); i++)
            {
               Attribute attribute = (Attribute) list.get( i );

               try
               {
                  setAttribute( attribute );
                  list.add( attribute );
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

         public MBeanInfo getMBeanInfo()
         {
            return info;
         }
      }

      class ConfigurableDataSource
         extends EditableConfiguration
      {
         private ServiceReference<DataSourceService> service;

         ConfigurableDataSource( ServiceReference<DataSourceService> service, MBeanInfo info, String identity, Map<String, QualifiedName> propertyNames )
         {
            super( info, identity, propertyNames );
            this.service = service;
         }

         public Object invoke( String s, Object[] objects, String[] strings ) throws MBeanException, ReflectionException
         {
            if (s.equals( "restart" ))
            {
               try
               {
                  // Refresh and restart
                  if (service.isActive())
                  {
                     ((Activatable) service).passivate();
                     ((Activatable) service).activate();
                  }

                  return "Service restarted";
               } catch (Exception e)
               {
                  return "Could not restart service:" + e.getMessage();
               }
            }

            return "Unknown operation";
         }

      }
   }
}
