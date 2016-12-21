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
package se.streamsource.streamflow.web.application.archival;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.web.infrastructure.scheduler.QuartzSchedulerService;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * TODO
 */
@Mixins(ArchivalService.Mixin.class)
public interface ArchivalService
      extends ServiceComposite, Configuration<ArchivalConfiguration>, Activatable
{

   abstract class Mixin
         implements ArchivalService, Activatable
   {
       @Service
       QuartzSchedulerService scheduler;

       private JobDetail startJob;
       private JobDetail stopJob;

      @This
      Configuration<ArchivalConfiguration> config;

      Logger logger = LoggerFactory.getLogger(ArchivalService.class);

       public void activate() throws Exception
       {
           if (config.configuration().enabled().get())
           {
               try
               {
                   startJob = newJob( ArchivalStartJob.class ).withIdentity( "archivalstartjob", "archivalgroup" ).build();
                   stopJob = newJob( ArchivalStopJob.class ).withIdentity( "archivalstopjob", "archivalgroup").build();

                   if( config.configuration().startScheduledArchival().get() )
                   {
                       Trigger startTrigger = newTrigger().withIdentity( "archivalstart", "archivalgroup" ).startNow()
                               .withSchedule(cronSchedule(config.configuration().startSchedule().get())).build();

                       Trigger stopTrigger = newTrigger().withIdentity("archivalstop", "archivalgroup" ).startNow()
                               .withSchedule( cronSchedule( config.configuration().stopSchedule().get())).build();

                       scheduler.scheduleJob(startJob, startTrigger );
                       scheduler.scheduleJob( stopJob, stopTrigger );

                   }
               } catch ( Exception e )
               {
                   startJob = null;
                   stopJob = null;
                   logger.error( "Unable to start ArchivalService: " + e.getMessage() );
               }
               if( config.configuration().startScheduledArchival().get() )
               {
                    logger.info( "Start activated: " + config.configuration().startSchedule().get() );
                    logger.info( "Stop activated: " + config.configuration().stopSchedule().get() );
               } else
               {
                   logger.info( "Activated: Manual mode only. " );
               }
           }
       }

       public void passivate() throws Exception
       {
           if( scheduler != null )
           {
               if( startJob != null )
               {
                    scheduler.deleteJob(startJob.getKey());
               }
               if( stopJob != null )
               {
                    scheduler.deleteJob( stopJob.getKey() );
               }
           }
           logger.info( "Passivated" );
       }
   }
}
