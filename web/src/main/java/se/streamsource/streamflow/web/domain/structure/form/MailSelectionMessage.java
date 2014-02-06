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
package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.common.Optional;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(MailSelectionMessage.Mixin.class)
public interface MailSelectionMessage
{
   void changeMailSelectionMessage( @Optional String newMessage );

   String getMailSelectionMessage();

   interface Data
   {
      @Optional
      Property<String> mailSelectionMessage();

      void changedMailSelectionMessage( @Optional DomainEvent event, @Optional String message );
   }

   abstract class Mixin
         implements MailSelectionMessage, Data
   {

      public String getMailSelectionMessage()
      {
         return mailSelectionMessage().get();
      }

      public void changeMailSelectionMessage( @Optional String newMessage )
      {
         changedMailSelectionMessage( null, newMessage );
      }

      public void changedMailSelectionMessage( @Optional DomainEvent event, @Optional String message )
      {
         mailSelectionMessage().set(  message );
      }
   }
}