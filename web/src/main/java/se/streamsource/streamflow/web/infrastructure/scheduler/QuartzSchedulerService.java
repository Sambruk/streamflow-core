/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.infrastructure.scheduler;

import java.util.Date;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 */
@Mixins(QuartzSchedulerService.Mixin.class)
public interface QuartzSchedulerService extends ServiceComposite, Activatable
{

   Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException;
   
   boolean deleteJob(JobKey jobKey) throws SchedulerException;

   boolean interruptJob( JobKey jobKey ) throws UnableToInterruptJobException;
   
   abstract class Mixin implements QuartzSchedulerService, Activatable
   {

      final Logger logger = LoggerFactory.getLogger( QuartzSchedulerService.class.getName() );

      @Structure
      Module module;

      @Service
      private Qi4JQuartzJobFactory jobFactory;
      
      private Scheduler scheduler;

      public void activate() throws Exception
      {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.setJobFactory( jobFactory );

            // and start it off
            scheduler.start();
      }

      public void passivate() throws Exception
      {
         scheduler.shutdown();
      }
      
      public Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException
      {
         return scheduler.scheduleJob( jobDetail, trigger );
      }

      public boolean deleteJob(JobKey jobKey) throws SchedulerException {
         return scheduler.deleteJob( jobKey );
      }

      public boolean interruptJob( JobKey jobKey ) throws UnableToInterruptJobException {
          return scheduler.interrupt( jobKey );
      }
   }
}
