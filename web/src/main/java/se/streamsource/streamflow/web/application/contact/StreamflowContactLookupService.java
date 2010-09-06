/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactLookup;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;
import se.streamsource.streamflow.web.domain.structure.caze.Contacts;

import java.util.ArrayList;
import java.util.List;

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

         if (!contactTemplate.name().get().equals(""))
            queryString.append( "name:\"" ).append( contactTemplate.name().get() ).append("\"");

         // TODO If no query string, then return empty list

         // TODO Match contacts
         queryString.append( " type:se.streamsource.streamflow.web.domain.entity.caze.CaseEntity" );
         queryString.append( " !status:DRAFT" );
         Query<Contacts.Data> cases = qbf
               .newNamedQuery( Contacts.Data.class, uowf.currentUnitOfWork(), "solrquery" ).setVariable( "query", queryString.toString() );

         ValueBuilder<ContactList> listBuilder = vbf.newValueBuilder( ContactList.class );
         for (Contacts.Data contact : cases)
         {
            // TODO Find case with contact
            for (se.streamsource.streamflow.domain.contact.ContactValue contactValue : contact.contacts().get())
            {
               if (!contactTemplate.name().get().equals("") && contactValue.name().equals(contactTemplate.name()))
                  listBuilder.prototype().contacts().get().add( vbf.newValueFromJSON( ContactValue.class, contactValue.toJSON()) );
            }
         }

         return listBuilder.newInstance();
      }
   }
}
