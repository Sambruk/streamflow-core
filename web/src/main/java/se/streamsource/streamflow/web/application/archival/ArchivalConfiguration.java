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
package se.streamsource.streamflow.web.application.archival;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;

/**
 * TODO
 */
public interface ArchivalConfiguration
   extends ConfigurationComposite, Enabled
{
    /**
     * Tells whether the scheduled archival job should be run automatically
     * or if we rely on manual archival.
     * @return
     */
    @UseDefaults
    Property<Boolean> startScheduledArchival();

    /**
     * The maximum time to live in days for a case.
     * After that the case will be archived as Pdf.
     * A value of -1 indicates for ever.
      * @return  The time to live in days for a case.
     */
   @UseDefaults
   Property<Integer> maxTimeToLive();

    /**
     * Amount of cases to process before making a 2 seconds wait.
     * @return
     */
    @UseDefaults
    Property<Integer> modulo();

    /**
     * A crontab schedule string for starting archival.
     *
     * @return
     */
    @UseDefaults
    Property<String> startSchedule();

    /**
     * A crontab schedule string for stopping archival.
     *
     * @return
     */
    @UseDefaults
    Property<String> stopSchedule();
}
