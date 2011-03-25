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

package se.streamsource.streamflow.web.application.contact;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactAddressValue;
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
      QueryBuilderFactory qbf;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

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

         ValueBuilder<ContactList> listBuilder = vbf.newValueBuilder( ContactList.class );

         if (argumentFound)
         {
            queryString.append( "type:se.streamsource.streamflow.web.domain.entity.caze.CaseEntity " );
            queryString.append( "!status:DRAFT" );
            Query<Contacts.Data> cases = qbf
                  .newNamedQuery( Contacts.Data.class, uowf.currentUnitOfWork(), "solrquery" ).setVariable( "query", queryString.toString() );


            se.streamsource.streamflow.domain.contact.ContactValue contactSearchCriteria = vbf.newValueFromJSON( se.streamsource.streamflow.domain.contact.ContactValue.class, contactTemplate.toJSON() );
            for (Contacts.Data contact : cases)
            {
               for (se.streamsource.streamflow.domain.contact.ContactValue contactValue : contact.contacts().get())
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
                        listBuilder.prototype().contacts().get().add( vbf.newValueFromJSON( ContactValue.class, contactValue.toJSON() ) );
                     }
                  }
               }
            }
         }
         return listBuilder.newInstance();
      }

      private boolean matchSearchResultWithQuery( se.streamsource.streamflow.domain.contact.ContactValue criteria, se.streamsource.streamflow.domain.contact.ContactValue result )
      {
         if (!criteria.name().get().isEmpty() && result.name().get().toLowerCase().contains( criteria.name().get().toLowerCase() ))
         {
            return true;
         }

         if (!criteria.phoneNumbers().get().isEmpty())
         {
            for (se.streamsource.streamflow.domain.contact.ContactPhoneValue phone : result.phoneNumbers().get())
            {
               if (!criteria.phoneNumbers().get().get( 0 ).phoneNumber().get().isEmpty() && phone.phoneNumber().get().contains( criteria.phoneNumbers().get().get( 0 ).phoneNumber().get() ))
                  return true;
            }
         }

         if (!criteria.addresses().get().isEmpty())
         {
            for (ContactAddressValue address : result.addresses().get())
            {
               if (!criteria.addresses().get().get( 0 ).address().get().isEmpty() && address.address().get().toLowerCase().contains( criteria.addresses().get().get( 0 ).address().get().toLowerCase() ))
                  return true;
            }
         }

         if (!criteria.emailAddresses().get().isEmpty())
         {
            for (se.streamsource.streamflow.domain.contact.ContactEmailValue email : result.emailAddresses().get())
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
