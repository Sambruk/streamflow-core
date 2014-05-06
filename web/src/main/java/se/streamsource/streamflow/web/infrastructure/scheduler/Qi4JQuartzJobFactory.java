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

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;


@Mixins(Qi4JQuartzJobFactory.Mixin.class)
public interface Qi4JQuartzJobFactory extends ServiceComposite, JobFactory
{
   abstract class Mixin implements Qi4JQuartzJobFactory, JobFactory
   {
      @Structure
      Module module;

      public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException
      {					
         TransientBuilder<? extends Job> newJobBuilder = module.transientBuilderFactory().newTransientBuilder( bundle.getJobDetail().getJobClass() );
         return newJobBuilder.newInstance();
      }
   }

}
