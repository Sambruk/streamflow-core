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
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.spi.Qi4jSPI;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * JMX Management for Streamflow. Exposes all configurable services as MBeans,
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
      Module module;

      @Structure
      Qi4jSPI spi;

      @Structure
      Application application;

      @Uses
      TransientBuilder<ManagerComposite> managerBuilder;

      @Uses
      ObjectBuilder<CompositeMBean> mbeanBuilder;

      public ObjectName objectName;
      public ManagerComposite manager;

      public void activate() throws Exception
      {
         ResourceBundle bundle = ResourceBundle.getBundle( Manager.class.getName() );

         Properties version = new Properties();
         version.load( getClass().getResourceAsStream( "/version.properties" ) );

         String versionString = version.getProperty( "application.name" ) + " " +
               version.getProperty( "application.version" ) + " build:" +
               version.getProperty( "application.buildNumber" ) + " revision:" +
               version.getProperty( "application.revision" );
         managerBuilder.prototype().version().set( versionString );

         manager = managerBuilder.newInstance();
         CompositeMBean mbean = mbeanBuilder.use( manager, Manager.class, bundle ).newInstance();

         manager.start();

         // Register the Model MBean in the MBean Server
         objectName = new ObjectName( "Qi4j:application="+application.name()+",name=Manager" );
         server.registerMBean( mbean, objectName );
      }
      public void passivate() throws Exception
      {
         manager.stop();

         server.unregisterMBean( objectName );
      }

      class ConfigurableService
            implements DynamicMBean
      {
         ServiceReference<Configuration> service;
         MBeanInfo info;
         String identity;
         Map<String, QualifiedName> propertyNames;

         ConfigurableService( ServiceReference<Configuration> service, MBeanInfo info, String identity, Map<String, QualifiedName> propertyNames )
         {
            this.service = service;
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

         public Object invoke( String s, Object[] objects, String[] strings ) throws MBeanException, ReflectionException
         {
            if (s.equals( "restart" ))
            {
               try
               {
                  // Refresh and restart
                  if (service.isActive())
                  {
                     // Refresh configuration
                     service.get().refresh();

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

         public MBeanInfo getMBeanInfo()
         {
            return info;
         }
      }
   }
}
