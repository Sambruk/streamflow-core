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
package se.streamsource.streamflow.web.application.mail;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;

/**
 * Configuration for the ReceiveMailService.
 */
public interface ReceiveMailConfiguration
      extends ConfigurationComposite, Enabled
{
   /**
    * SSL enabled
    * @return
    */
   @UseDefaults
   Property<Boolean> useSSL();

   /**
    * TTLS enabled
    * @return
    */
   @UseDefaults
   Property<Boolean> useTLS();

   /**
    * Mail debug enabled
    * @return
    */
   @UseDefaults
   Property<Boolean> debug();
   
   /**
    * The mailbox user.
    * @return
    */
   @UseDefaults
   Property<String> user();

   /**
    * Mailbox password
    * @return
    */
   @UseDefaults
   Property<String> password();

   /**
    *  The sleep period before checking inbox again (in minutes).
    * @return
    */
   @UseDefaults
   Property<Long> sleepPeriod();

   /**
    * The protocol to use - pop3 or imap
    * @return
    */
   @UseDefaults
   Property<String> protocol();

   /**
    *  The receiver host
    * @return
    */
   @UseDefaults
   Property<String> host();

   /**
    * The receiver port
    * @return
    */
   @UseDefaults
   Property<Integer> port();

   /**
    *  A boolean telling whether mails should be purged on the server or not.
    */
   @UseDefaults
   Property<Boolean> deleteMailOnInboxClose();

   /**
    * The name of the archive folder on the mail server
    * @return A string property
    */
   @UseDefaults
   Property<String> archiveFolder();
}
