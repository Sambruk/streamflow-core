/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.context.account;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;

/**
 * JAVADOC
 */
public class ProfileContext
{
   @Uses MessageRecipient recipient;
   @Uses MessageRecipient.Data recipientData;

   public void changemessagedeliverytype( @Name("messagedeliverytype") MessageRecipient.MessageDeliveryTypes newDeliveryType )
   {
      recipient.changeMessageDeliveryType( newDeliveryType );
   }

   public String messagedeliverytype()
   {
      return recipientData.delivery().get().name();
   }

}
