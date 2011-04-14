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

import org.qi4j.api.common.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactAddressDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.web.application.contact.StreamflowContactLookupService;
import se.streamsource.dci.api.ServiceAvailable;

/**
 * JAVADOC
 */
public class ContactableContext
      implements IndexContext<ContactDTO>
{
   @Optional
   @Service
   StreamflowContactLookupService contactLookup;

   @Structure
   ValueBuilderFactory vbf;

   public ContactDTO index()
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactDTO contact = contactable.getContact();
      return contact;
   }

   public void changename( StringValue name )
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactDTO contact = contactable.getContact();

      ValueBuilder<ContactDTO> builder = contact.buildWith();
      builder.prototype().name().set( name.string().get().trim() );

      contactable.updateContact( builder.newInstance() );
   }

   public void changenote( StringValue note )
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactDTO contact = contactable.getContact();

      ValueBuilder<ContactDTO> builder = contact.buildWith();
      builder.prototype().note().set( note.string().get() );

      contactable.updateContact( builder.newInstance() );
   }

   public void changecontactid( StringValue contactId )
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactDTO contact = contactable.getContact();

      ValueBuilder<ContactDTO> builder = contact.buildWith();
      builder.prototype().contactId().set( contactId.string().get() );

      contactable.updateContact( builder.newInstance() );
   }

   public void changecompany( StringValue company )
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactDTO contact = contactable.getContact();

      ValueBuilder<ContactDTO> builder = contact.buildWith();
      builder.prototype().company().set( company.string().get() );

      contactable.updateContact( builder.newInstance() );
   }

   public void changephonenumber( ContactPhoneDTO phoneDTO)
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactDTO contact = contactable.getContact();

      ValueBuilder<ContactDTO> builder = contact.buildWith();

      // Create an empty phone value if it doesnt exist already
      if (contact.phoneNumbers().get().isEmpty())
      {
         ContactPhoneDTO phone = vbf.newValue( ContactPhoneDTO.class ).<ContactPhoneDTO>buildWith().prototype();
         phone.phoneNumber().set( phoneDTO.phoneNumber().get() );
         builder.prototype().phoneNumbers().get().add( phone );
      } else
      {
         builder.prototype().phoneNumbers().get().get( 0 ).phoneNumber().set( phoneDTO.phoneNumber().get() );
      }

      contactable.updateContact( builder.newInstance() );
   }

   public void changeaddress( ContactAddressDTO addressDTO)
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactDTO contact = contactable.getContact();

      ValueBuilder<ContactDTO> builder = contact.buildWith();

      // Create an empty address value if it doesnt exist already
      if (contact.addresses().get().isEmpty())
      {
         ContactAddressDTO address = vbf.newValue( ContactAddressDTO.class ).<ContactAddressDTO>buildWith().prototype();
         address.address().set( addressDTO.address().get() );
         builder.prototype().addresses().get().add( address );
      } else
      {
         builder.prototype().addresses().get().get( 0 ).address().set( addressDTO.address().get() );
      }

      contactable.updateContact( builder.newInstance() );
   }

   public void changeemailaddress( ContactEmailDTO emailDTO)
   {
      Contactable contactable = RoleMap.role( Contactable.class );
      ContactDTO contact = contactable.getContact();

      ValueBuilder<ContactDTO> builder = contact.buildWith();

      // Create an empty email value if it doesnt exist already
      if (contact.emailAddresses().get().isEmpty())
      {
         ContactEmailDTO email = vbf.newValue( ContactEmailDTO.class ).<ContactEmailDTO>buildWith().prototype();
         email.emailAddress().set( emailDTO.emailAddress().get().trim() );
         builder.prototype().emailAddresses().get().add( email );
      } else
      {
         builder.prototype().emailAddresses().get().get( 0 ).emailAddress().set( emailDTO.emailAddress().get().trim() );
      }

      contactable.updateContact( builder.newInstance() );
   }

   @ServiceAvailable(StreamflowContactLookupService.class)
   public ContactList contactlookup( se.streamsource.streamflow.server.plugin.contact.ContactValue template )
   {
      if (contactLookup != null)
         return contactLookup.lookup( template );
      else
         return vbf.newValue( ContactList.class );
   }
}