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

import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.web.infrastructure.scheduler.QuartzSchedulerService;

/**
 * Responsible for interrupting ArchivalStartJob.
 */
@Mixins(ArchivalStopJob.Mixin.class)
public interface ArchivalStopJob extends Job, TransientComposite
{
    boolean interruptArchival() throws UnableToInterruptJobException;

    abstract class Mixin implements ArchivalStopJob
    {
        @Service
        QuartzSchedulerService scheduler;

        Logger logger = LoggerFactory.getLogger(ArchivalStopJob.class);

        public boolean interruptArchival() throws UnableToInterruptJobException {
            return scheduler.interruptJob( JobKey.jobKey( "archivalstartjob", "archivalgroup" ));
        }

        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            try
            {
                logger.info("Interrupting archival of cases");
                interruptArchival();
            } catch (Throwable e)
            {
                logger.error("Could not interrupt archival of cases", e);
            }
        }
    }
}
