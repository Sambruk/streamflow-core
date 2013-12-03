/**
 *
 * Copyright 2009-2013 Jayway Products AB
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

import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.dci.api.SkipResourceValidityCheck;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactBuilder;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPreference;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactsDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetSearchDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.StreetsDTO;
import se.streamsource.streamflow.server.plugin.address.StreetList;
import se.streamsource.streamflow.server.plugin.address.StreetValue;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactLookup;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caze.Contacts;
import se.streamsource.streamflow.web.infrastructure.plugin.address.StreetAddressLookupService;
import se.streamsource.streamflow.web.infrastructure.plugin.contact.ContactLookupService;

import java.util.List;

/**
 * JAVADOC
 */
public class ContactContext
      implements DeleteContext
{
   @Structure
   Module module;

   @Optional
   @Service
   ServiceReference<ContactLookupService> contactLookup;

   @Optional
   @Service
   ServiceReference<StreetAddressLookupService> streetLookup;

   public ContactDTO index()
   {
      Contacts.Data contacts = RoleMap.role( Contacts.Data.class );
      Integer index = RoleMap.role( Integer.class );
      
      return contacts.contacts().get().get( index );
   }

   @RequiresPermission(PermissionType.write)
   public void delete()
   {
      Contacts contacts = RoleMap.role( Contacts.class );
      Integer index = RoleMap.role( Integer.class );

      contacts.deleteContact( index );
   }

   @RequiresPermission(PermissionType.write)
   public void update(@Optional @Name("name") String name,
                      @Optional @Name("contactId") String contactId,
                      @Optional @Name("company") String company,
                      @Optional @Name("iscompany") Boolean isCompany,
                      @Optional @Name("phone") String phone,
                      @Optional @Name("email") String email,
                      @Optional @Name("address") String address,
                      @Optional @Name("zipCode") String zip,
                      @Optional @Name("city") String city,
                      @Optional @Name("region") String region,
                      @Optional @Name("country") String country,
                      @Optional @Name("contactpreference") String contactPreference,
                      @Optional @Name("note") String note)
   {
      Contacts contacts = RoleMap.role( Contacts.class );
      Integer index = RoleMap.role( Integer.class );
      ContactDTO contact = RoleMap.role( ContactDTO.class );

      ContactBuilder builder = new ContactBuilder(contact, module.valueBuilderFactory());

      if (name != null)
         builder.name(name );
      if (contactId != null)
         builder.contactId( contactId );
      if (company != null)
         builder.company( company );
      if (isCompany != null)
         builder.isCompany( isCompany );
      if (phone != null)
         builder.phoneNumber( phone );
      if (email != null)
         builder.email( email );
      if (address != null)
         builder.address( address );
      if (zip != null)
         builder.zipCode( zip );
      if (city != null)
         builder.city( city );
      if (region != null)
         builder.region( region );
      if (country != null)
         builder.country( country );
      if ( contactPreference != null )
      {
         try
         {
            builder.contactPreference( ContactPreference.valueOf( contactPreference ) );
         } catch (IllegalArgumentException e ) {
            builder.contactPreference( null );
         }
      }
      if (note != null)
         builder.note(note);

      contacts.updateContact( index, builder.newInstance() );
   }

   @ServiceAvailable( service = ContactLookupService.class, availability = true )
   @SkipResourceValidityCheck
   @RequiresPermission(PermissionType.write)
   public ContactsDTO searchcontacts()
   {
      // This method has to convert between the internal ContactDTO and the plugin API ContactDTO,
      // hence the use of JSON as intermediary
      ContactDTO contact = RoleMap.role( ContactDTO.class );
      se.streamsource.streamflow.server.plugin.contact.ContactValue pluginContact = module.valueBuilderFactory().newValueFromJSON(se.streamsource.streamflow.server.plugin.contact.ContactValue.class, contact.toJSON());
      ValueBuilder<ContactsDTO> builder = module.valueBuilderFactory().newValueBuilder(ContactsDTO.class);

      try
      {
         if (contactLookup != null)
         {
            ContactLookup lookup = contactLookup.get();
            ContactList possibleContacts = lookup.lookup( pluginContact );
            List<ContactDTO> contactList = builder.prototype().contacts().get();

            for (se.streamsource.streamflow.server.plugin.contact.ContactValue possibleContact : possibleContacts.contacts().get())
            {
               contactList.add( module.valueBuilderFactory().newValueFromJSON(ContactDTO.class, possibleContact.toJSON()) );
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
   

   @ServiceAvailable( service = StreetAddressLookupService.class, availability = true )
   @SkipResourceValidityCheck
   @RequiresPermission(PermissionType.write)
   public StreetsDTO searchstreets(StreetSearchDTO search)
   {

      ValueBuilder<StreetValue> builder = module.valueBuilderFactory().newValueBuilder(StreetValue.class);
      builder.prototype().address().set( search.address().get() );
      ValueBuilder<StreetsDTO> resultBuilder = module.valueBuilderFactory().newValueBuilder( StreetsDTO.class );
      try
      {
         if (streetLookup != null)
         {
            StreetAddressLookupService lookup = streetLookup.get();
            StreetList streetList = lookup.lookup( builder.newInstance() );
            List<StreetSearchDTO> streets = resultBuilder.prototype().streets().get();
            
            for (StreetValue street : streetList.streets().get())
            {
               streets.add( module.valueBuilderFactory().newValueFromJSON( StreetSearchDTO.class, street.toJSON() ) );
            }
            return resultBuilder.newInstance();
         } else
         {
            return resultBuilder.newInstance();
         }
      } catch (ServiceImporterException e)
      {
         // Not available at this time
         return resultBuilder.newInstance();
      }
   }
}
