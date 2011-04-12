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

package se.streamsource.streamflow.web.domain.structure.conversation;

import org.qi4j.api.common.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.web.domain.entity.user.*;

/**
 * JAVADOC
 */
@Mixins(MessageReceiver.Mixin.class)
public interface MessageReceiver
{
   void receiveMessage( Message message );

   interface Data
   {
      void receivedMessage( @Optional DomainEvent event, Message message );
   }

   class Mixin
         implements MessageReceiver, Data
   {

      @This
      Identity identity;

      public void receiveMessage( Message message )
      {
         if (!identity.identity().get().equals(
               EntityReference.getEntityReference(
                     ((Message.Data) message).sender().get() ).identity() ) && !identity.identity().get().equals(UserEntity.ADMINISTRATOR_USERNAME))
         {
            receivedMessage( null, message );
         }
      }

      public void receivedMessage( DomainEvent event, Message message )
      {
         // No-op. Use event listeners to perform actual delivery
      }
   }
}
