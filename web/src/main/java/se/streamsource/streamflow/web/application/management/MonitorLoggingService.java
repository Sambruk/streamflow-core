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

import org.apache.log4j.FileAppender;
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
@Mixins(MonitorLoggingService.Mixin.class)
public interface MonitorLoggingService
   extends ServiceComposite, Activatable
{
   class Mixin
      implements Activatable
   {
      @Service
      FileConfiguration fileConfig;

      public void activate() throws Exception
      {
         Logger logger = Logger.getLogger( MonitorLoggingService.class );

         File monitorDirectory = new File(fileConfig.logDirectory(), "monitor");
         monitorDirectory.mkdirs();

         File restQueryLog = new File(monitorDirectory, "query.log");
         Logger.getLogger( "monitor.rest.query" ).addAppender( new FileAppender(new PatternLayout("%m%n"), restQueryLog.getAbsolutePath() ));

         logger.info( "Logging query performance to:"+restQueryLog );
      }

      public void passivate() throws Exception
      {
      }
   }
}
