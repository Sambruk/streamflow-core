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
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.infrastructure.event.domain.source.helper.TransactionTrackerConfiguration;

/**
 * Configuration for the SendMailService.
 */
public interface SendMailConfiguration
      extends TransactionTrackerConfiguration
{
   /**
    * Smtp host address
    *
    * @return
    */
   @UseDefaults
   Property<String> host();

   /**
    * The server port
    * @return
    */
   @UseDefaults
   Property<String> port();

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
    * The from address.
    * @return
    */
   @UseDefaults
   Property<String> from();

   /**
    * The from name.
    * @return
    */
   @UseDefaults
   Property<String> fromName();

   /**
    * Use of authentication.
    * @return true or false
    */
   @UseDefaults
   Property<Boolean> authentication();

   @UseDefaults
   Property<String> replyTo();

}
