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

package se.streamsource.streamflow.web.context.workspace.cases.contact;

import org.qi4j.api.common.*;
import org.qi4j.api.constraint.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.service.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.api.workspace.cases.contact.*;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactsDTO;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactLookup;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.streamflow.web.domain.structure.caze.Contacts;
import se.streamsource.streamflow.web.infrastructure.plugin.contact.ContactLookupService;

import java.util.*;

/**
 * JAVADOC
 */
public class ContactContext
      implements DeleteContext
{
   @Structure
   ValueBuilderFactory vbf;

   @Optional
   @Service
   ServiceReference<ContactLookupService> contactLookup;

   public void delete()
   {
      Contacts contacts = RoleMap.role( Contacts.class );
      Integer index = RoleMap.role( Integer.class );

      contacts.deleteContact( index );
   }

   public void update(@Optional @Name("name") String name, @Optional @Name("email") String email, @Optional @Name("phone") String phone)
   {
      Contacts contacts = RoleMap.role( Contacts.class );
      Integer index = RoleMap.role( Integer.class );
      ContactDTO contact = RoleMap.role( ContactDTO.class );

      ContactBuilder builder = new ContactBuilder(contact, vbf);

      if (name != null)
         builder.name(name );
      if (email != null)
         builder.email( email );
      if (phone != null)
         builder.phoneNumber(phone);

      contacts.updateContact( index, builder.newInstance() );
   }

   public void changename( StringValue name )
   {
      update(name.string().get(), null, null);
   }

   public void changenote( StringValue note )
   {
      Contacts contacts = RoleMap.role( Contacts.class );
      Integer index = RoleMap.role( Integer.class );
      ContactDTO contact = RoleMap.role( ContactDTO.class );

      ValueBuilder<ContactDTO> builder = contact.buildWith();
      builder.prototype().note().set( note.string().get() );
      contacts.updateContact( index, builder.newInstance() );
   }

   public void changecontactid( StringValue contactId )
   {
      Contacts contacts = RoleMap.role( Contacts.class );
      Integer index = RoleMap.role( Integer.class );
      ContactDTO contact = RoleMap.role( ContactDTO.class );

      ValueBuilder<ContactDTO> builder = contact.buildWith();
      builder.prototype().contactId().set( contactId.string().get() );
      contacts.updateContact( index, builder.newInstance() );
   }

   public void changecompany( StringValue company )
   {
      Contacts contacts = RoleMap.role( Contacts.class );
      Integer index = RoleMap.role( Integer.class );
      ContactDTO contact = RoleMap.role( ContactDTO.class );

      ValueBuilder<ContactDTO> builder = contact.buildWith();
      builder.prototype().company().set( company.string().get() );
      contacts.updateContact( index, builder.newInstance() );
   }

   public void changephonenumber( ContactPhoneDTO phoneDTO)
   {
      Contacts contacts = RoleMap.role( Contacts.class );
      Integer index = RoleMap.role( Integer.class );
      ContactDTO contact = RoleMap.role( ContactDTO.class );

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

      contacts.updateContact( index, builder.newInstance() );
   }

   public void changeaddress( ContactAddressDTO addressDTO)
   {
      Contacts contacts = RoleMap.role( Contacts.class );
      Integer index = RoleMap.role( Integer.class );
      ContactDTO contact = RoleMap.role( ContactDTO.class );

      ValueBuilder<ContactDTO> builder = contact.buildWith();

      // Create an empty address value if it doesnt exist already
      if (contact.addresses().get().isEmpty())
      {
         ContactAddressDTO address = vbf.newValue( ContactAddressDTO.class ).<ContactAddressDTO>buildWith().prototype();
         address.address().set( addressDTO.address().get() );
         builder.prototype().addresses().get().add( address );
      } else
      {
         builder.prototype().addresses().get().set( 0, addressDTO );
      }

      contacts.updateContact( index, builder.newInstance() );
   }

   public void changeemailaddress( ContactEmailDTO emailDTO)
   {
      Contacts contacts = RoleMap.role( Contacts.class );
      Integer index = RoleMap.role( Integer.class );
      ContactDTO contact = RoleMap.role( ContactDTO.class );

      ValueBuilder<ContactDTO> builder = contact.buildWith();

      // Create an empty email value if it doesnt exist already
      if (contact.emailAddresses().get().isEmpty())
      {
         ContactEmailDTO email = vbf.newValue( ContactEmailDTO.class ).<ContactEmailDTO>buildWith().prototype();
         email.emailAddress().set( emailDTO.emailAddress().get() );
         builder.prototype().emailAddresses().get().add( email );
      } else
      {
         builder.prototype().emailAddresses().get().get( 0 ).emailAddress().set( emailDTO.emailAddress().get() );
      }


      contacts.updateContact( index, builder.newInstance() );
   }

   @ServiceAvailable(ContactLookupService.class)
   public ContactsDTO searchcontacts()
   {
      // This method has to convert between the internal ContactDTO and the plugin API ContactDTO,
      // hence the use of JSON as intermediary
      ContactDTO contact = RoleMap.role( ContactDTO.class );
      se.streamsource.streamflow.server.plugin.contact.ContactValue pluginContact = vbf.newValueFromJSON( se.streamsource.streamflow.server.plugin.contact.ContactValue.class, contact.toJSON() );
      ValueBuilder<ContactsDTO> builder = vbf.newValueBuilder( ContactsDTO.class );

      try
      {
         if (contactLookup != null)
         {
            ContactLookup lookup = contactLookup.get();
            ContactList possibleContacts = lookup.lookup( pluginContact );
            List<ContactDTO> contactList = builder.prototype().contacts().get();

            for (se.streamsource.streamflow.server.plugin.contact.ContactValue possibleContact : possibleContacts.contacts().get())
            {
               contactList.add( vbf.newValueFromJSON( ContactDTO.class, possibleContact.toJSON() ) );
            }
            return builder.newInstance();
         } else
         {
            return builder.newInstance();
         }
      } catch (ServiceImporterException e)
      {
         // Not available at this time
         return builder.newInstance();
      }
   }
}
