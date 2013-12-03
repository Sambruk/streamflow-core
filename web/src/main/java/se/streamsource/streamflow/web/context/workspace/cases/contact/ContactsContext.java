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

import static se.streamsource.streamflow.api.workspace.cases.CaseStates.DRAFT;
import static se.streamsource.streamflow.api.workspace.cases.CaseStates.OPEN;

import java.util.List;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactsDTO;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.domain.entity.RequiresRemoved;
import se.streamsource.streamflow.web.domain.interaction.gtd.RequiresStatus;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caze.Contacts;

/**
 * JAVADOC
 */
public class ContactsContext
   implements IndexContext<ContactsDTO>
{
   @Structure
   Module module;

   public ContactsDTO index()
   {
      ValueBuilder<ContactsDTO> builder = module.valueBuilderFactory().newValueBuilder( ContactsDTO.class );
      ValueBuilder<ContactDTO> contactBuilder = module.valueBuilderFactory().newValueBuilder( ContactDTO.class );
      List<ContactDTO> list = builder.prototype().contacts().get();

      Contacts.Data contacts = RoleMap.role( Contacts.Data.class );

      for (ContactDTO contact : contacts.contacts().get())
      {
         contactBuilder.prototype().company().set( contact.company().get() );
         contactBuilder.prototype().name().set( contact.name().get() );
         contactBuilder.prototype().isCompany().set( contact.isCompany().get() );
         contactBuilder.prototype().note().set( contact.note().get() );
         contactBuilder.prototype().picture().set( contact.picture().get() );
         contactBuilder.prototype().contactId().set( contact.contactId().get() );
         contactBuilder.prototype().addresses().set( contact.addresses().get() );
         contactBuilder.prototype().emailAddresses().set( contact.emailAddresses().get() );
         contactBuilder.prototype().phoneNumbers().set( contact.phoneNumbers().get() );
         contactBuilder.prototype().contactPreference().set( contact.contactPreference().get() );
         list.add( contactBuilder.newInstance() );
      }
      return builder.newInstance();
   }

   @RequiresStatus({OPEN, DRAFT})
   @RequiresRemoved(false)
   @RequiresPermission(PermissionType.write)
   public void add( ContactDTO newContact )
   {
      Contacts contacts = RoleMap.role( Contacts.class );
      contacts.addContact( newContact );
   }
}