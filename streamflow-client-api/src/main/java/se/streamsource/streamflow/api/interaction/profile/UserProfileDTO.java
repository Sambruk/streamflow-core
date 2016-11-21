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
package se.streamsource.streamflow.api.interaction.profile;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;

/**
 *
 */
public interface UserProfileDTO
   extends ContactDTO
{
   /**
    * Mark as read timeout in seconds.
    * @return The amount of seconds that have to pass until unread flag is set to false.
    */
   @UseDefaults
   Property<Long> markReadTimeout();

   /**
    * The preferred message delivery type for the user.
    * @return A message delivery type.
    */
   @UseDefaults
   Property<String> messageDeliveryType();

   /**
    * A mail footer to be attached to mails sent by the user.
    * @return A string representing a mail footer.
    */
   @UseDefaults
   Property<String> mailFooter();
}
