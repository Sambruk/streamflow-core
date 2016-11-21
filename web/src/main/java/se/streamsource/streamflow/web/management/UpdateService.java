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
package se.streamsource.streamflow.web.management;

import java.beans.PropertyVetoException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.service.ServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.infrastructure.NamedThreadFactory;
import se.streamsource.streamflow.web.application.defaults.AvailabilityService;

/**
 * TODO
 */
@Mixins(UpdateService.Mixin.class)
public interface UpdateService
      extends ServiceComposite, Configuration, Activatable
{
   abstract class Mixin
         implements UpdateService, Activatable, Runnable
   {
      @Structure
      Application app;

      @Structure
      Module module;

      @This
      Configuration<UpdateConfiguration> config;

      @Uses
      ServiceDescriptor descriptor;

      @Service
      AvailabilityService availabilityService;

      private ExecutorService executor;
      //private HistoryCleanup cleanup;

      private boolean wasOn;

      Logger log;

      public void activate() throws Exception
      {
         log = LoggerFactory.getLogger( UpdateService.class );

         executor = Executors.newSingleThreadExecutor( new NamedThreadFactory( "UpdateMigration" ) );
         executor.submit( this );
         // As of 1.7.5 remove history cleanup
         //executor.submit( cleanup = module.objectBuilderFactory().newObject( HistoryCleanup.class ) );
         log.info( "Activate: Executer submitted." );
      }

      public void passivate() throws Exception
      {
         //cleanup.stopAndDiscard();
         executor.shutdown();

         log.info( "Passivate: Executor shut down." );
      }

      public void run()
      {
         Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

         // make rest api unavailable during operation
         wasOn = availabilityService.getCircuitBreaker().isOn();
         availabilityService.getCircuitBreaker().trip();

         UpdateBuilder builder = descriptor.metaInfo( UpdateBuilder.class );
         String version = app.version();
         String lastVersion = config.configuration().lastStartupVersion().get();

         // Run general rules if version has changed
         if (!version.equals(lastVersion))
         {
            Iterable<UpdateRule> rules = builder.getRules().getRules(lastVersion, version);
            try
            {
               if (rules != null)
               {
                  for (UpdateRule rule : rules)
                  {
                     rule.getOperation().update(app, module);
                     log.debug(rule.toString());
                  }

                  log.info("Updated to " + version);
               }

               config.configuration().lastStartupVersion().set(version);
               config.save();
            } catch (Exception e)
            {
               log.error("Update failed. Aborting! Try fixing the problem and start again", e);
             }
         }
         try
         {
            // if rest api was on previously make it available again
            if( wasOn )
               availabilityService.getCircuitBreaker().turnOn();
         } catch (PropertyVetoException e)
         {
            log.error( "Could not turn on availability circuit breaker.", e );
         }
      }
   }
}
