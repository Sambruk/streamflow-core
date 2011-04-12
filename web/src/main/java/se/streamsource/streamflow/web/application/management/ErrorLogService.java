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

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.service.*;
import org.qi4j.api.structure.*;

import javax.management.*;
import javax.management.modelmbean.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

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

      @Structure
      Application application;

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

         objectName = new ObjectName( "Qi4j:application="+application.name()+",class=Log,name=errorlog" );
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