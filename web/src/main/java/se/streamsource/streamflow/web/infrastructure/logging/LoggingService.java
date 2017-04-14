/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.infrastructure.logging;

import java.io.File;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

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

         // Entity export
         File entityExportDirectory = new File(fileConfig.logDirectory(), "entityexport");
         entityExportDirectory.mkdirs();

         File entityExportLog = new File(entityExportDirectory, "entityexport.log");
         final Logger entityExportLogger = Logger.getLogger( "se.streamsource.streamflow.web.application.entityexport.EntityExportService" );
         entityExportLogger.addAppender( new DailyRollingFileAppender(new PatternLayout("%d - %m%n"), entityExportLog.getAbsolutePath(), "'.'yyyy-ww" ));
         entityExportLogger.setAdditivity( false );
         logger.info( "Logging command performance to:"+entityExportLog );

         // Access logging
         File accessLog = new File(fileConfig.logDirectory(), "access.log");
         final Logger accessLogger = Logger.getLogger( "LogService" );
         accessLogger.addAppender( new DailyRollingFileAppender(new PatternLayout("%d %m%n"), accessLog.getAbsolutePath(), "'.'yyyy-ww" ));
         accessLogger.setAdditivity( false );
         logger.info( "Logging HTTP access to:"+accessLog );

         // SPARQL query logging
         File sparqlLog = new File(fileConfig.logDirectory(), "sparql.log");
         final Logger sparqlLogger = Logger.getLogger( "org.qi4j.index.rdf.query.internal.RdfQueryParserImpl" );
         sparqlLogger.addAppender( new DailyRollingFileAppender(new PatternLayout("%m%n%n"), sparqlLog.getAbsolutePath(), "'.'yyyy-ww" ));
         sparqlLogger.setAdditivity( false );
         logger.info( "Logging SPARQL queries to:"+sparqlLog );

         // General logging
         File generalLog = new File(fileConfig.logDirectory(), "streamflow.log");
         Logger.getRootLogger().addAppender( new DailyRollingFileAppender(new PatternLayout("%d %5p %c{1} - %m%n"), generalLog.getAbsolutePath(), "'.'yyyy-ww" ));
         logger.info( "Logging Streamflow messages:"+generalLog );

      }

      public void passivate() throws Exception
      {
      }
   }
}
