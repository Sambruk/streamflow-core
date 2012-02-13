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

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;


public interface SystemDefaultsConfiguration
      extends ConfigurationComposite, Enabled
{
   /**
    *  The default sort order configuration for case table content.
    * @return A boolean
    */
   @UseDefaults
   Property<Boolean> sortOrderAscending();

   @UseDefaults
   Property<Boolean> caseLogSystemVisible();

   @UseDefaults
   Property<Boolean> caseLogSystemTraceVisible();

   @UseDefaults
   Property<Boolean> caseLogCustomVisible();

   @UseDefaults
   Property<Boolean> caseLogContactVisible();

   @UseDefaults
   Property<Boolean> caseLogFormVisible();

   @UseDefaults
   Property<Boolean> caseLogConversationVisible();

   @UseDefaults
   Property<Boolean> caseLogAttachmentVisible();

   /**
    * The name of the organizational unit responsible for mail receiver support.
    * @return A string property
    */
   @UseDefaults
   Property<String> supportOrganizationName();

   /**
    *  The name of the support project responsible bor mail receiver support.
    * @return
    */
   @UseDefaults
   Property<String> supportProjectName();

   /**
    * The name of the support case type marking mail receiver cases.
    * @return
    */
   @UseDefaults
   Property<String> supportCaseTypeForIncomingEmailName();
}
