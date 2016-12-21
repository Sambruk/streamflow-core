/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.application.contact;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.streamflow.api.workspace.cases.contact.ContactAddressDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.server.plugin.contact.ContactEmailValue;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactLookup;
import se.streamsource.streamflow.server.plugin.contact.ContactPhoneValue;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;
import se.streamsource.streamflow.web.domain.structure.caze.Contacts;

/**
 * JAVADOC
 */
@Mixins(StreamflowContactLookupService.Mixin.class)
public interface StreamflowContactLookupService
      extends ServiceComposite, ContactLookup
{
   class Mixin
         implements ContactLookup
   {
      @Structure
      Module module;

      public ContactList lookup( ContactValue contactTemplate )
      {

         StringBuilder queryString = new StringBuilder();

         boolean argumentFound = false;
         if (!contactTemplate.name().get().equals( "" ))
         {
            queryString.append( "name:\"" ).append( contactTemplate.name().get() ).append( "\" " );
            argumentFound = true;
         }
         if (!contactTemplate.contactId().get().equals( "" ))
         {
            queryString.append( "contactId:\"" ).append( contactTemplate.contactId().get() ).append( "\" " );
            argumentFound = true;
         }
         if (contactTemplate.phoneNumbers().get().size() > 0)
         {
            argumentFound = true;
            queryString.append( "phoneNumber:(" );
            String or = "";
            for (ContactPhoneValue phone : contactTemplate.phoneNumbers().get())
            {
               queryString.append( or ).append( "\"" ).append( phone.phoneNumber().get() ).append( "\"" );
               or = " OR ";
            }
            queryString.append( ") " );
         }
         if (contactTemplate.emailAddresses().get().size() > 0)
         {
            argumentFound = true;
            queryString.append( "emailAddress:(" );
            String or = "";
            for (ContactEmailValue email : contactTemplate.emailAddresses().get())
            {
               queryString.append( or ).append( "\"" ).append( email.emailAddress().get() ).append( "\"" );
               or = " OR ";
            }
            queryString.append( ") " );
         }

         ValueBuilder<ContactList> listBuilder = module.valueBuilderFactory().newValueBuilder(ContactList.class);

         if (argumentFound)
         {
            queryString.append( "type:se.streamsource.streamflow.web.domain.entity.caze.CaseEntity " );
            queryString.append( "!status:DRAFT" );
            Query<Contacts.Data> cases = module.queryBuilderFactory()
                  .newNamedQuery(Contacts.Data.class, module.unitOfWorkFactory().currentUnitOfWork(), "solrquery").setVariable( "query", queryString.toString() );


            ContactDTO contactSearchCriteria = module.valueBuilderFactory().newValueFromJSON(ContactDTO.class, contactTemplate.toJSON());
            for (Contacts.Data contact : cases)
            {
               for (ContactDTO contactValue : contact.contacts().get())
               {
                  if (!contactValue.equals( contactSearchCriteria ))
                  {
                     boolean matched = true;
                     if (contact.contacts().get().size() > 1)
                     {
                        matched = matchSearchResultWithQuery( contactSearchCriteria, contactValue );
                     }
                     if (matched)
                     {
                        listBuilder.prototype().contacts().get().add( module.valueBuilderFactory().newValueFromJSON(ContactValue.class, contactValue.toJSON()) );
                     }
                  }
               }
            }
         }
         return listBuilder.newInstance();
      }

      private boolean matchSearchResultWithQuery( ContactDTO criteria, ContactDTO result )
      {
         if (!criteria.name().get().isEmpty() && result.name().get().toLowerCase().contains( criteria.name().get().toLowerCase() ))
         {
            return true;
         }

         if (!criteria.phoneNumbers().get().isEmpty())
         {
            for (ContactPhoneDTO phone : result.phoneNumbers().get())
            {
               if (!criteria.phoneNumbers().get().get( 0 ).phoneNumber().get().isEmpty() && phone.phoneNumber().get().contains( criteria.phoneNumbers().get().get( 0 ).phoneNumber().get() ))
                  return true;
            }
         }

         if (!criteria.addresses().get().isEmpty())
         {
            for (ContactAddressDTO address : result.addresses().get())
            {
               if (!criteria.addresses().get().get( 0 ).address().get().isEmpty() && address.address().get().toLowerCase().contains( criteria.addresses().get().get( 0 ).address().get().toLowerCase() ))
                  return true;
            }
         }

         if (!criteria.emailAddresses().get().isEmpty())
         {
            for (ContactEmailDTO email : result.emailAddresses().get())
            {
               if (!criteria.emailAddresses().get().get( 0 ).emailAddress().get().isEmpty() && email.emailAddress().get().toLowerCase().contains( criteria.emailAddresses().get().get( 0 ).emailAddress().get().toLowerCase() ))
                  return true;
            }
         }

         if (!criteria.contactId().get().isEmpty() && result.contactId().get().toLowerCase().contains( criteria.contactId().get().toLowerCase() ))
         {
            return true;
         }
         return false;
      }
   }
}
