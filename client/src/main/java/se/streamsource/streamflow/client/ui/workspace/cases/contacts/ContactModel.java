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

package se.streamsource.streamflow.client.ui.workspace.cases.contacts;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.util.*;
import org.qi4j.api.value.*;
import org.restlet.resource.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.Links;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactAddressDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactsDTO;

/**
 * Model for a contact of a case
 */
public class ContactModel
{
   @Uses
   private ContactDTO contact;

   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;


   public ContactDTO getContact()
   {
      return contact;
   }

   public ContactPhoneDTO getPhoneNumber()
   {
      return contact.phoneNumbers().get().get( 0 );
   }

   public ContactAddressDTO getAddress()
   {
      return contact.addresses().get().get( 0 );
   }

   public ContactEmailDTO getEmailAddress()
   {
      return contact.emailAddresses().get().get( 0 );
   }

   public void changeName( String newName ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( newName );
      client.putCommand( "changename", builder.newInstance() );
   }

   public void changeNote( String newNote ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( newNote );
      client.putCommand( "changenote", builder.newInstance() );
   }

   public void changeContactId( String newId ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( newId );
      client.putCommand( "changecontactid", builder.newInstance() );
   }

   public void changeCompany( String newCompany ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( newCompany );
      client.putCommand( "changecompany", builder.newInstance() );
   }

   public void changePhoneNumber( String newPhoneNumber ) throws ResourceException
   {
      ValueBuilder<ContactPhoneDTO> builder = vbf.newValueBuilder( ContactPhoneDTO.class );
      builder.prototype().phoneNumber().set( newPhoneNumber );
      client.putCommand( "changephonenumber", builder.newInstance() );
   }

   public void changeAddress( String newAddress ) throws ResourceException
   {
      getAddress().address().set( newAddress );
      client.putCommand( "changeaddress", getAddress() );
   }

   public void changeZipCode(String newZipCode)
   {
      getAddress().zipCode().set( newZipCode );
      client.putCommand( "changeaddress", getAddress() );
   }

   public void changeCity(String newCity)
   {
      getAddress().city().set( newCity );
      client.putCommand( "changeaddress", getAddress() );
   }

   public void changeRegion(String newRegion)
   {
      getAddress().region().set( newRegion );
      client.putCommand( "changeaddress", getAddress() );
   }

   public void changeCountry(String newCountry)
   {
      getAddress().country().set( newCountry );
      client.putCommand( "changeaddress", getAddress() );
   }

   public void changeEmailAddress( String newEmailAddress ) throws ResourceException
   {
      ValueBuilder<ContactEmailDTO> builder = vbf.newValueBuilder( ContactEmailDTO.class );
      builder.prototype().emailAddress().set( newEmailAddress );
      client.putCommand( "changeemailaddress", builder.newInstance() );
   }

   public boolean isContactLookupEnabled()
   {
      ResourceValue resource = client.queryResource();
      return Iterables.matchesAny( Links.withRel("searchcontacts"), resource.queries().get() );
   }

   public ContactsDTO searchContacts( ContactDTO query ) throws ResourceException
   {
      return client.query( "searchcontacts", query, ContactsDTO.class );
   }
}