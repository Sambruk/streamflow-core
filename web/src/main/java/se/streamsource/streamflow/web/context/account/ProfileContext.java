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

package se.streamsource.streamflow.web.context.account;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;

/**
 * JAVADOC
 */
public class ProfileContext
{
   @Structure
   ValueBuilderFactory vbf;

   public void changemessagedeliverytype( StringValue newDeliveryType )
   {
      MessageRecipient recipient = RoleMap.role( MessageRecipient.class );

      if (MessageRecipient.MessageDeliveryTypes.email.toString().equals(
            newDeliveryType.string().get() ))
      {
         recipient
               .changeMessageDeliveryType( MessageRecipient.MessageDeliveryTypes.email );
      } else
      {
         recipient
               .changeMessageDeliveryType( MessageRecipient.MessageDeliveryTypes.none );
      }
   }

   public String messagedeliverytype()
   {
      MessageRecipient.Data recipientData = RoleMap.role( MessageRecipient.Data.class );
      return recipientData.delivery().get().name();
   }

}
