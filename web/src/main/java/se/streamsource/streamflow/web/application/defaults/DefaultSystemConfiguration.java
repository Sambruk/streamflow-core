/**
 *
 * Copyright 2009-2011 Streamsource AB
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
package se.streamsource.streamflow.web.application.defaults;

import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;


public interface DefaultSystemConfiguration
      extends ConfigurationComposite, Enabled
{
   Property<Boolean> sortOrderAscending();
   
   Property<Boolean> caseLogSystemVisible();

   Property<Boolean> caseLogSystemTraceVisible();

   Property<Boolean> caseLogCustomVisible();

   Property<Boolean> caseLogContactVisible();

   Property<Boolean> caseLogFormVisible();

   Property<Boolean> caseLogConversationVisible();

   Property<Boolean> caseLogAttachmentVisible();
}
