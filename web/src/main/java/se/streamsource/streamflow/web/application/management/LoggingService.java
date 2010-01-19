/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

import java.io.File;

/**
 * JAVADOC
 */
@Mixins(LoggingService.Mixin.class)
public interface LoggingService
   extends ServiceComposite, Activatable
{
   class Mixin
      implements Activatable
   {
      @Service
      FileConfiguration fileConfig;

      public void activate() throws Exception
      {
         Logger logger = Logger.getLogger( LoggingService.class );

         // Monitors
         File monitorDirectory = new File(fileConfig.logDirectory(), "monitor");
         monitorDirectory.mkdirs();

         File restQueryLog = new File(monitorDirectory, "query.log");
         Logger.getLogger( "monitor.rest.query" ).addAppender( new DailyRollingFileAppender(new PatternLayout("%m%n"), restQueryLog.getAbsolutePath(), "'.'yyyy-ww" ));
         logger.info( "Logging query performance to:"+restQueryLog );

         File restCommandLog = new File(monitorDirectory, "command.log");
         Logger.getLogger( "monitor.rest.command" ).addAppender( new DailyRollingFileAppender(new PatternLayout("%m%n"), restCommandLog.getAbsolutePath(), "'.'yyyy-ww" ));
         logger.info( "Logging command performance to:"+restCommandLog );

         // Access logging
         File accessLog = new File(fileConfig.logDirectory(), "access.log");
         Logger.getLogger( "org.restlet.Component.LogService" ).addAppender( new DailyRollingFileAppender(new PatternLayout("%m%n"), accessLog.getAbsolutePath(), "'.'yyyy-ww" ));
         logger.info( "Logging HTTP access to:"+accessLog );

         // General logging
         File generalLog = new File(fileConfig.logDirectory(), "streamflow.log");
         Logger.getRootLogger().addAppender( new DailyRollingFileAppender(new PatternLayout("%5p %c{1} - %m%n"), generalLog.getAbsolutePath(), "'.'yyyy-ww" ));
         logger.info( "Logging StreamFlow messages:"+generalLog );

      }

      public void passivate() throws Exception
      {
      }
   }
}
