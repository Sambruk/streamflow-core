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
package se.streamsource.streamflow.client.ui.account;

import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.api.interaction.profile.UserProfileDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.client.ResourceModel;

/**
 * JAVADOC
 */
public class ProfileModel
      extends ResourceModel<UserProfileDTO>
{

   public void changeMessageDeliveryType( String newDeliveryType )
         throws ResourceException
   {
      Form form = new Form();
      form.set("messagedeliverytype", newDeliveryType);
      client.putCommand( "changemessagedeliverytype", form.getWebRepresentation() );
   }

   /*public String getMessageDeliveryType()
   {
      return profile.messageDeliveryType().get();
   }


   public UserProfileDTO getProfile()
   {
      return profile;
   }
*/
   public ContactPhoneDTO getPhoneNumber()
   {
      if (getIndex().phoneNumbers().get().isEmpty())
      {
         return module.valueBuilderFactory().newValue(ContactPhoneDTO.class)
               .<ContactPhoneDTO>buildWith().prototype();
      }
      return getIndex().phoneNumbers().get().get( 0 );
   }

   public ContactEmailDTO getEmailAddress()
   {
      if (getIndex().emailAddresses().get().isEmpty())
      {
         return module.valueBuilderFactory().newValue(ContactEmailDTO.class)
               .<ContactEmailDTO>buildWith().prototype();
      }
      return getIndex().emailAddresses().get().get( 0 );
   }

   public void changeName( String newName )
   {
      Form form = new Form();
      form.set("name", newName);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changePhoneNumber( String newPhoneNumber )
   {
      Form form = new Form();
      form.set("phone", newPhoneNumber);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeEmailAddress( String newEmailAddress )
   {
      Form form = new Form();
      form.set("email", newEmailAddress);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeMarkReadTimeout( String newMarkReadTimeout )
   {
      Form form = new Form();
      form.set("markreadtimeoutsec", newMarkReadTimeout);
      client.putCommand( "changemarkreadtimeout", form.getWebRepresentation() );
   }

   public void changeMailFooter( String newMailFooter )
   {
      Form form = new Form();
      form.set("mailfooter", newMailFooter);
      client.putCommand( "changemailfooter", form.getWebRepresentation() );
   }
}
