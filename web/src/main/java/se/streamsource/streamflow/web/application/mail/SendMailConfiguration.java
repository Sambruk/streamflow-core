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

package se.streamsource.streamflow.web.application.mail;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.configuration.Enabled;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.TransactionTrackerConfiguration;

/**
 * Configuration for the SendMailService.
 */
public interface SendMailConfiguration
      extends ConfigurationComposite, Enabled, TransactionTrackerConfiguration
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
}
