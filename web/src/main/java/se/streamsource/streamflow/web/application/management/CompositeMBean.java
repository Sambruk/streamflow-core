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
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.spi.Qi4jSPI;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Expose a TransientComposite as an MBean. All properties are used as JMX attributes. The rest
 * of the methods are exposed as operations.
 */
public class CompositeMBean
      implements DynamicMBean
{
   private TransientComposite composite;

   private StateHolder state;

   private Map<String, QualifiedName> names = new HashMap<String, QualifiedName>();

   private MBeanInfo info;
   private List<Method> operationMethods = new ArrayList<Method>();

   public CompositeMBean( @Uses TransientComposite composite, @Uses Class exposedInterface, @Uses final ResourceBundle resourceBundle, @Structure Qi4jSPI spi )
   {
      String description = resourceBundle.getString( "mbean.description" );

      // Find state
      final List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
      state = spi.getState( composite );
      state.visitProperties( new StateHolder.StateVisitor()
      {
         public void visitProperty( QualifiedName name, Object value )
         {
            Property property = state.getProperty( name );
            MBeanAttributeInfo attribute = new MBeanAttributeInfo( property.qualifiedName().name(),
                  property.type().toString(),
                  resourceBundle.getString( property.qualifiedName().name() + ".description" ),
                  true, !(property.isImmutable() || property.isComputed()), false );
            names.put( name.name(), name );
            attributes.add( attribute );
         }
      } );

      List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();
      for (Method method : exposedInterface.getMethods())
      {
         if (!method.getReturnType().equals( Property.class ))
         {
            List<MBeanParameterInfo> parameters = new ArrayList<MBeanParameterInfo>();
            int idx = 1;
            for (Class<?> parameterType : method.getParameterTypes())
            {
               String name = resourceBundle.getString( method.getName() + ".parameter" + idx + ".name" );
               String paramDescription = resourceBundle.getString( method.getName() + ".parameter" + idx + ".description" );
               String type = parameterType.getName();
               MBeanParameterInfo paramInfo = new MBeanParameterInfo( name, type, paramDescription );
               parameters.add( paramInfo );
               idx++;
            }

            String impactType = resourceBundle.getString( method.getName() + ".impact" );
            try
            {
               MBeanOperationInfo operation = new MBeanOperationInfo( method.getName(),
                     resourceBundle.getString( method.getName() + ".description" ),
                     parameters.toArray( new MBeanParameterInfo[parameters.size()] ),
                     method.getReturnType().getName(),
                     MBeanOperationInfo.class.getField( impactType ).getInt( null ) );
               operationMethods.add( method );
               operations.add( operation );
            } catch (Exception e)
            {
               throw new IllegalArgumentException( "Unknown impact type:" + impactType );
            }
         }
      }

      info = new MBeanInfo( exposedInterface.getName(),
            description,
            attributes.toArray( new MBeanAttributeInfo[attributes.size()] ),
            new MBeanConstructorInfo[0],
            operations.toArray( new MBeanOperationInfo[operations.size()] ),
            new MBeanNotificationInfo[0] );

      this.composite = composite;
   }

   public Object getAttribute( String name ) throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      QualifiedName name1 = names.get( name );
      if (name1 == null)
         throw new AttributeNotFoundException( name );
      Property prop = state.getProperty( name1 );

      return prop.get();
   }

   public void setAttribute( Attribute attribute ) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      QualifiedName qualifiedName = names.get( attribute.getName() );
      if (qualifiedName == null)
         throw new AttributeNotFoundException( attribute.getName() );
      Property prop = state.getProperty( qualifiedName );
      prop.set( attribute.getValue() );
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

   public Object invoke( String s, Object[] arguments, String[] parameterTypes ) throws MBeanException, ReflectionException
   {
      Method method = getOperation( s, parameterTypes );

      try
      {
         return method.invoke( composite, arguments );
      } catch (IllegalAccessException e)
      {
         throw new ReflectionException( e );
      } catch (InvocationTargetException e)
      {
         throw new MBeanException( (Exception) e.getCause() );
      }
   }

   private Method getOperation( String s, String[] parameterTypes )
   {
      nextoperation:
      for (int idx = 0; idx < info.getOperations().length; idx++)
      {
         MBeanOperationInfo mBeanOperationInfo = info.getOperations()[idx];
         if (mBeanOperationInfo.getName().equals( s ))
         {
            MBeanParameterInfo[] params = mBeanOperationInfo.getSignature();
            for (int i = 0; i < parameterTypes.length; i++)
            {
               String parameterType = parameterTypes[i];
               if (params.length < i || !parameterType.equals( params[i].getType() ))
                  continue nextoperation;
            }

            return operationMethods.get( idx );
         }
      }

      return null;
   }

   public MBeanInfo getMBeanInfo()
   {
      return info;
   }
}
