/**
 *
 * Copyright 2009-2010 Streamsource AB
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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.event.source.EventStream;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.RequiredModelMBean;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Service for exposing Logger errors JMX.
 */
@Mixins(ErrorLogService.Mixin.class)
public interface ErrorLogService
      extends Activatable, ServiceComposite
{
   class Mixin
         extends Handler
         implements Activatable
   {
      Map<String, String> sourceMappings = new ConcurrentHashMap<String, String>();

      @Service
      EventStream stream;

      @Service
      MBeanServer server;
      public ObjectName objectName;

      long seq = 0;
      RequiredModelMBean mbean;
      ExecutorService executor;

      public void activate() throws Exception
      {
         ModelMBeanNotificationInfo[] notificationInfos = new ModelMBeanNotificationInfo[1];
         notificationInfos[0] = new ModelMBeanNotificationInfo( new String[]{"web", "other"}, "Errorlog", "Errorlog" );
         ModelMBeanInfo info = new ModelMBeanInfoSupport( ErrorLogService.class.getName(),
               "Errorlog",
               null, null, null, notificationInfos );
         mbean = new RequiredModelMBean( info );

         objectName = new ObjectName( "Streamflow:type=Log,name=errorlog" );
         server.registerMBean( mbean, objectName );

         setLevel( Level.SEVERE );

         Logger.getLogger( "" ).addHandler( this );

         // From Logger name -> JMX source name
         sourceMappings.put( "command", "web" );

         executor = Executors.newSingleThreadExecutor();
      }

      public void passivate() throws Exception
      {
         Logger.getLogger( "" ).removeHandler( this );
         executor.shutdown();
         server.unregisterMBean( objectName );
         objectName = null;
      }

      public void publish( LogRecord record )
      {
         if (!isLoggable( record ) || objectName == null)
            return;

         String loggerName = record.getLoggerName();
         String source = null;
         if (loggerName != null)
            source = sourceMappings.get( loggerName );
         if (source == null)
            source = "other"; // No mapping found

         final Notification notification = new Notification( source, objectName, seq++, record.getMillis(), record.getMessage() );
         Throwable throwable = record.getThrown();
         if (throwable != null)
         {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter( writer );
            throwable.printStackTrace( printWriter );
            printWriter.close();
            notification.setUserData( writer.toString() );
         }

         executor.submit( new Runnable()
         {
            public void run()
            {

               try
               {
                  mbean.sendNotification( notification );
               } catch (MBeanException e)
               {
                  e.printStackTrace();
               }
            }
         } );
      }

      public void flush()
      {
      }

      public void close() throws SecurityException
      {
      }
   }

}