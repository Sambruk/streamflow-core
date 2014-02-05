/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.web.application.dueon;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

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

/**
 * TODO
 */
@Mixins(DueOnNotificationService.Mixin.class)
public interface DueOnNotificationService extends ServiceComposite, Configuration<DueOnNotificationConfiguration>,
      Activatable
{

   abstract class Mixin implements DueOnNotificationService, Activatable
   {

      final Logger logger = LoggerFactory.getLogger( DueOnNotificationService.class.getName() );

      @This
      Configuration<DueOnNotificationConfiguration> config;

      @Service
      QuartzSchedulerService scheduler;

      private JobDetail job;

      public void activate() throws Exception
      {
         if (config.configuration().enabled().get())
         {
            try
            {
                // define the job and tie it to our HelloJob class
                job = newJob( DueOnNotificationJob.class ).withIdentity( "dueOnJob", "schedulergroup" ).build();

                // Trigger the job to run now, and then repeat every 40 seconds
                Trigger trigger = newTrigger().withIdentity( "dueontrigger", "schedulergroup" ).startNow()
                      .withSchedule( cronSchedule( config.configuration().schedule().get() ) ).build();

                // Tell quartz to schedule the job using our trigger
                scheduler.scheduleJob( job, trigger );
            } catch ( Exception e )
            {
                job = null;
                logger.error( "Unable to start DueOnNotificationService: " + e.getMessage() );
            }
            logger.info( "Activated: " + config.configuration().schedule().get() );
         }
      }

      public void passivate() throws Exception
      {
         if( scheduler != null && job != null )
         {
            scheduler.deleteJob( job.getKey() );
         }
         logger.info( "Passivated" );
      }

   }
}
