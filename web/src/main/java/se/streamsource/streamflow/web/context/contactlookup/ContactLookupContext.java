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
package se.streamsource.streamflow.web.context.contactlookup;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;
import se.streamsource.streamflow.web.application.contact.StreamflowContactLookupService;

/**
 * The context for the contact lookup used mainly by the reference plugin implementation.
 */
public class ContactLookupContext
{
   @Structure
   Module module;

   @Optional
   @Service
   StreamflowContactLookupService contactLookup;

   @ServiceAvailable( service = StreamflowContactLookupService.class, availability = true )
   public ContactList contactlookup( ContactValue template )
   {
      if (contactLookup != null)
         return contactLookup.lookup( template );
      else
         return module.valueBuilderFactory().newValue( ContactList.class );
   }
}
