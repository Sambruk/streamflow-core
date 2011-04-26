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

package se.streamsource.streamflow.api.workspace.cases.contact;

import org.qi4j.api.value.*;

import java.util.*;

/**
 * Builder for ContactDTO. Can be used during creation or updating of existing contact
 */
public class ContactBuilder
{
   private ValueBuilder<ContactDTO> contactBuilder;
   private ValueBuilderFactory vbf;

   public ContactBuilder(ContactDTO contact, ValueBuilderFactory vbf)
   {
      this.vbf = vbf;
      contactBuilder =contact.buildWith();
   }

   public ContactBuilder(ValueBuilderFactory vbf)
   {
      this.vbf = vbf;
      contactBuilder = vbf.newValueBuilder(ContactDTO.class);
   }

   public ContactBuilder name(String name)
   {
      contactBuilder.prototype().name().set(name);
      return this;
   }

   public ContactBuilder company(String company)
   {
      contactBuilder.prototype().company().set(company);
      return this;
   }

   public ContactBuilder note(String note)
   {
      contactBuilder.prototype().note().set(note);
      return this;
   }

   public ContactBuilder phoneNumber(String phoneNumber)
   {
      List<ContactPhoneDTO> phoneNumbers = contactBuilder.prototype().phoneNumbers().get();
      phoneNumbers.clear();
      ValueBuilder<ContactPhoneDTO> phoneBuilder = vbf.newValueBuilder(ContactPhoneDTO.class);
      phoneBuilder.prototype().phoneNumber().set(phoneNumber);
      phoneNumbers.add(phoneBuilder.newInstance());

      return this;
   }

   public ContactBuilder address(String address)
   {
      List<ContactAddressDTO> addresses = contactBuilder.prototype().addresses().get();
      if (addresses.isEmpty())
      {
         ValueBuilder<ContactAddressDTO> addressBuilder = vbf.newValueBuilder(ContactAddressDTO.class);
         addresses.add(addressBuilder.prototype());
      }

      addresses.get(0).address().set(address);

      return this;
   }

   public ContactBuilder email(String email)
   {
      List<ContactEmailDTO> addresses = contactBuilder.prototype().emailAddresses().get();
      addresses.clear();
      ValueBuilder<ContactEmailDTO> addressBuilder = vbf.newValueBuilder(ContactEmailDTO.class);
      addressBuilder.prototype().emailAddress().set(email);
      addresses.add(addressBuilder.newInstance());

      return this;
   }

   public ContactDTO newInstance()
   {
      return contactBuilder.newInstance();
   }

   public ContactBuilder contactId(String id)
   {
      contactBuilder.prototype().contactId().set(id);
      return this;
   }

   public ContactBuilder isCompany(boolean isCompany)
   {
      contactBuilder.prototype().isCompany().set(isCompany);
      return this;
   }

   public ContactBuilder zipCode(String zip)
   {
      List<ContactAddressDTO> addresses = contactBuilder.prototype().addresses().get();
      if (addresses.isEmpty())
      {
         ValueBuilder<ContactAddressDTO> addressBuilder = vbf.newValueBuilder(ContactAddressDTO.class);
         addresses.add(addressBuilder.prototype());
      }

      addresses.get(0).zipCode().set(zip);

      return this;
   }

   public ContactBuilder city(String city)
   {
      List<ContactAddressDTO> addresses = contactBuilder.prototype().addresses().get();
      if (addresses.isEmpty())
      {
         ValueBuilder<ContactAddressDTO> addressBuilder = vbf.newValueBuilder(ContactAddressDTO.class);
         addresses.add(addressBuilder.prototype());
      }

      addresses.get(0).city().set(city);

      return this;
   }

   public ContactBuilder region(String region)
   {
      List<ContactAddressDTO> addresses = contactBuilder.prototype().addresses().get();
      if (addresses.isEmpty())
      {
         ValueBuilder<ContactAddressDTO> addressBuilder = vbf.newValueBuilder(ContactAddressDTO.class);
         addresses.add(addressBuilder.prototype());
      }

      addresses.get(0).region().set(region);

      return this;
   }

   public ContactBuilder country(String country)
   {
      List<ContactAddressDTO> addresses = contactBuilder.prototype().addresses().get();
      if (addresses.isEmpty())
      {
         ValueBuilder<ContactAddressDTO> addressBuilder = vbf.newValueBuilder(ContactAddressDTO.class);
         addresses.add(addressBuilder.prototype());
      }

      addresses.get(0).country().set(country);

      return this;
   }
}
