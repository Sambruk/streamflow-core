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

package se.streamsource.streamflow.web.application.management;

import org.apache.log4j.*;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.*;
import org.qi4j.api.common.*;
import org.qi4j.api.composite.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.property.*;
import org.qi4j.spi.*;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Expose a TransientComposite as an MBean. All properties are used as JMX attributes. The rest
 * of the methods are exposed as operations.
 */
public class CompositeMBean
      extends NotificationBroadcasterSupport
      implements DynamicMBean
{
   private TransientComposite composite;

   private StateHolder state;

   private Map<String, QualifiedName> names = new HashMap<String, QualifiedName>();

   private MBeanInfo info;
   private List<Method> operationMethods = new ArrayList<Method>();
   private AppenderSkeleton appender;
   private Class exposedInterface;

   private ExecutorService executor;


   public CompositeMBean( @Uses final TransientComposite composite, @Uses final Class exposedInterface, @Uses final ResourceBundle resourceBundle, @Structure Qi4jSPI spi )
   {
      this.exposedInterface = exposedInterface;
      String description = resourceBundle.getString( "mbean.description" );

      // Find state
      final List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
      state = spi.getState( composite );
      state.visitProperties( new StateHolder.StateVisitor<RuntimeException>()
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
            new MBeanNotificationInfo[]{new MBeanNotificationInfo(new String[]{"info","warning","error"}, "Log", "Log")} );

      this.composite = composite;

      appender = new AppenderSkeleton()
      {
         long seq = 0;

         @Override
         protected void append( LoggingEvent event )
         {
            final Notification notification = new Notification( event.getLevel().toString(), event.getLoggerName(), seq++, event.getTimeStamp(), event.getMessage() == null ? null : event.getMessage().toString() );
            ThrowableInformation throwable = event.getThrowableInformation();
            if (throwable != null)
            {
               StringWriter writer = new StringWriter();
               PrintWriter printWriter = new PrintWriter( writer );
               throwable.getThrowable().printStackTrace( printWriter );
               printWriter.close();
               notification.setUserData( writer.toString() );
            }

            sendNotification( notification );
         }

         public void close()
         {
            Logger.getLogger( exposedInterface ).removeAppender( this );
         }

         public boolean requiresLayout()
         {
            return false;
         }
      };

      executor = Executors.newSingleThreadExecutor();
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

   public synchronized Object invoke( String s, final Object[] arguments, String[] parameterTypes ) throws MBeanException, ReflectionException
   {
      final Method method = getOperation( s, parameterTypes );

      final Callable<Object> call = new Callable<Object>()
         {
         public Object call() throws Exception
         {
            try
            {
               Logger.getRootLogger().addAppender( appender );
               return method.invoke( composite, arguments );
            } catch (IllegalAccessException e)
            {
               throw new ReflectionException( e );
            } catch (InvocationTargetException e)
            {
               throw new MBeanException( (Exception) e.getCause() );
            } finally
            {
               Logger.getRootLogger().removeAppender( appender );
            }
         }
      };

      if (method.getReturnType().equals(Void.TYPE))
      {
         executor.submit(new Callable<Object>()
         {
            public Object call() throws Exception
            {
               try
               {
                  return call.call();
               } catch (Exception e)
               {
                  // Log exception
                  Logger.getRootLogger().addAppender( appender );
                  LoggerFactory.getLogger( exposedInterface ).error( "Could not complete "+method.getName(), e );
                  Logger.getRootLogger().removeAppender( appender );
                  throw e;
               }
            }
         });
         return method.getName()+" started. See notification log for details";
      } else
      {
         try
         {
            return call.call();
         } catch (Exception e)
         {
            throw new ReflectionException(e);
         }
      }
   }

   private Method getOperation( String s, String[] parameterTypes ) throws ReflectionException
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

      throw new ReflectionException(new NoSuchMethodException(s));
   }

   @Override
   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return info.getNotifications();
   }

   public MBeanInfo getMBeanInfo()
   {
      return info;
   }
}
