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

   @UseDefaults
   Property<String> mapDefaultStartLocation();

   @UseDefaults
   Property<Integer> mapDefaultZoomLevel();

   @UseDefaults
   Property<String> mapquestReverseLookupUrlPattern();

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

   /**
    * The name of the support case type for send mail failures
    * @return
    */
   @UseDefaults
   Property<String> supportCaseTypeForOutgoingEmailName();

   /**
    * The base url for Surface Webforms
    * @return
    */
   @UseDefaults
   Property<String> webFormsProxyUrl();

   /**
    * Tells whether to search in notes time line or not.
    * @return a boolean
    */
   @UseDefaults
   Property<Boolean> includeNotesInSearch();

   @UseDefaults
   Property<Long> defaultMarkReadTimeout();

    /**
     * The map url as a MessageFormat pattern
     * f.ex. <a href=\"http://maps.google.com/maps?z=13&t=m&q={0}\" alt=\"Google Maps\">Klicka här för att visa karta</a>
     * @return
     */
    @UseDefaults
    Property<String> mapDefaultUrlPattern();

   /**
    * The base URL for opening cases directly in the webclient
    * f.ex. http://<host>/webclient/#/cases/
    * @return  The base URL as String
    */
   @UseDefaults
   Property<String> webclientBaseUrl();
}
