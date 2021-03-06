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
package se.streamsource.streamflow.api.workspace.cases.general;

import org.joda.time.DateTime;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.constraints.annotation.Matches;

/**
 * Contains identification, address and other necessary info to be able to
 * send a link to a person/legal body for picking up a form draft.
 */
public interface SecondSigneeInfoValue
   extends ValueComposite
{
   @UseDefaults
   Property<Boolean> singlesignature();

   @Optional
   @UseDefaults
   Property<String> name();

   @Optional
   @UseDefaults
   @Matches("([\\d]{12})?")
   Property<String> socialsecuritynumber();

   @Optional
   @UseDefaults
   @Matches("\\+?[\\d -]*")
   Property<String> phonenumber();

   @UseDefaults
   @Matches("(.*@.*)?")
   Property<String> email();

   @Optional
   @UseDefaults
   Property<String> secondsigneetaskref();

   @Optional
   Property<DateTime> lastReminderSent();

   @Optional
   @UseDefaults
   Property<String> secondDraftUrl();
}
