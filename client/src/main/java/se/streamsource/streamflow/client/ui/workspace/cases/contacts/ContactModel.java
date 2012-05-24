/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.Links;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactAddressDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPreference;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactsDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetsDTO;

import java.util.List;

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
   Module module;

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
      Form form = new Form();
      form.set("name", newName);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeNote( String newNote ) throws ResourceException
   {
      Form form = new Form();
      form.set("note", newNote);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeContactId( String newId ) throws ResourceException
   {
      Form form = new Form();
      form.set("contactId", newId);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeCompany( String newCompany ) throws ResourceException
   {
      Form form = new Form();
      form.set("company", newCompany);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changePhoneNumber( String newPhone ) throws ResourceException
   {
      Form form = new Form();
      form.set("phone", newPhone);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeAddress( String newAddress ) throws ResourceException
   {
      Form form = new Form();
      form.set("address", newAddress);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeZipCode(String newZipCode)
   {
      Form form = new Form();
      form.set("zipCode", newZipCode);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeCity(String newCity)
   {
      Form form = new Form();
      form.set("city", newCity);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeRegion(String newRegion)
   {
      Form form = new Form();
      form.set("region", newRegion);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeCountry(String newCountry)
   {
      Form form = new Form();
      form.set("country", newCountry);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeEmailAddress( String newEmailAddress ) throws ResourceException
   {
      Form form = new Form();
      form.set("email", newEmailAddress);
      client.putCommand( "update", form.getWebRepresentation() );
   }

   public void changeAddressAndCity( String newAddress, String newCity)
   {
      Form form = new Form();
      form.set("address", newAddress);
      form.set("city", newCity);
      client.putCommand( "update", form.getWebRepresentation() );
   }
   
   public boolean isContactLookupEnabled()
   {
      ResourceValue resource = client.query();
      return Iterables.matchesAny( Links.withRel("searchcontacts"), resource.queries().get() );
   }

   public void initMissingValues() {
      
   }
   public ContactsDTO searchContacts( ContactDTO query ) throws ResourceException
   {
      return client.query( "searchcontacts", ContactsDTO.class, query);
   }

   public boolean isStreetLookupEnabled()
   {
      ResourceValue resource = client.query();
      return Iterables.matchesAny( Links.withRel("searchstreets"), resource.queries().get() );
   }

   public StreetsDTO searchStreets( String query ) throws ResourceException
   {
      ValueBuilder<StreetSearchDTO> builder = module.valueBuilderFactory().newValueBuilder( StreetSearchDTO.class );
      builder.prototype().address().set( query );
      return client.query( "searchstreets", StreetsDTO.class, builder.newInstance());
   }

   public void changeContactPreference( ContactPreference preference )
   {
      Form form = new Form();
      if ( preference == null )
      {
         form.set( "contactpreference", "" );
      } else
      {
         form.set( "contactpreference", preference.name() );
      }
      client.putCommand( "update", form.getWebRepresentation() );
   }
}