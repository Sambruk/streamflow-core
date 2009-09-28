/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.application.migration;

import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * Configuration of the startup migration service
 */
public interface StartupMigrationConfiguration
    extends ConfigurationComposite
{
    /**
     * Keep track of the version the application had during the last startup.
     * Only perform migration if the version was different from the current one.
     *
     * @return the last startup version
     */
    @Optional
    Property<String> lastStartupVersion();
}
