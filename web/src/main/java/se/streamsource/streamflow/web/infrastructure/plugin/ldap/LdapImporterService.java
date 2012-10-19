/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.infrastructure.plugin.ldap;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.service.ServiceDescriptor;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;
import se.streamsource.infrastructure.circuitbreaker.service.ServiceCircuitBreaker;
import se.streamsource.streamflow.web.infrastructure.plugin.LdapImporterServiceConfiguration;
import se.streamsource.streamflow.web.infrastructure.scheduler.QuartzSchedulerService;

import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;

/**
 * This service takes care of ldap imports via ldap plugin.
 */
@Mixins( LdapImporterService.Mixin.class )
public interface LdapImporterService
      extends ServiceComposite, Configuration, Activatable, ServiceCircuitBreaker
{
   public Configuration<LdapImporterServiceConfiguration> getConfiguration();

   abstract class Mixin
      implements LdapImporterService
   {
      private static final Logger logger = LoggerFactory.getLogger( LdapImporterService.class );

      @Uses
      ServiceDescriptor descriptor;

      @This
      Configuration<LdapImporterServiceConfiguration> config;

      CircuitBreaker circuitBreaker;

      @Service
      QuartzSchedulerService scheduler;

      @Structure
      Module module;

      private JobDetail job;

      public void activate() throws Exception
      {
         circuitBreaker = descriptor.metaInfo( CircuitBreaker.class );

         config.configuration();

         if (config.configuration().enabled().get())
         {
            // define the job and tie it to our Job class
            job = newJob( LdapImportJob.class ).withIdentity( "ldapImportJob", "schedulergroup" ).build();
            job.getJobDataMap().put( "config", config );
            job.getJobDataMap().put( "module", module );

            // Trigger the job to run with cron schedule provided from configuration.
            Trigger trigger = newTrigger().withIdentity( "ldapimporttrigger", "schedulergroup" ).startNow()
                  .withSchedule( cronSchedule( config.configuration().schedule().get() ) ).build();

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob( job, trigger );
         }
      }

      public void passivate() throws Exception
      {
         if( scheduler != null && job != null )
         {
            scheduler.deleteJob( job.getKey() );
            logger.info( "Deleted scheduled job " + job.getKey() );
         }
         logger.info( "Passivated" );
      }

      public CircuitBreaker getCircuitBreaker()
      {
         return circuitBreaker;
      }

      public Configuration<LdapImporterServiceConfiguration> getConfiguration()
      {
         return config;
      }
   }
}
