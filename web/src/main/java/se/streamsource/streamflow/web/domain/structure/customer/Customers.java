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
package se.streamsource.streamflow.web.domain.structure.customer;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.streamflow.api.ContactId;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactBuilder;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.server.plugin.contact.ContactList;
import se.streamsource.streamflow.server.plugin.contact.ContactValue;
import se.streamsource.streamflow.web.domain.entity.customer.CustomerEntity;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;
import se.streamsource.streamflow.web.infrastructure.plugin.contact.ContactLookupService;

/**
 * This class represents customer to the system. A customer can be identified by
 * many different ways i.e. Id (Personnummer), emailadress.
 * 
 */
@Mixins(Customers.Mixin.class)
public interface Customers
{
   Customer createCustomerById(@ContactId String contactId, String name) throws IllegalArgumentException;

   interface Data
   {
      Customer createdCustomerById(@Optional DomainEvent event, String contactId);
   }

   abstract class Mixin implements Customers, Data
   {
      @Structure
      Module module;

      @Service
      IdentityGenerator idGen;

      @Optional @Service
      ContactLookupService contactLookup;

      public Customer createCustomerById(String contactId, String name) throws IllegalArgumentException
      {
         // Check if customer already exist
         StringBuilder queryBuilder = new StringBuilder();
         queryBuilder.append( " type:se.streamsource.streamflow.web.domain.entity.customer.CustomerEntity" );
         queryBuilder.append( " contactId:" + contactId );
         Query<Customer> query = module.queryBuilderFactory()
               .newNamedQuery( Customer.class, module.unitOfWorkFactory().currentUnitOfWork(), "solrquery" )
               .setVariable( "query", queryBuilder.toString() );
         if (query.iterator().hasNext())
         {
            throw new IllegalArgumentException( "customer_already_exists" );
         }

         Customer customer = createdCustomerById( null, idGen.generate( CustomerEntity.class ) );
         if( contactLookup != null )
         {
            ValueBuilder<ContactValue> contactValueBuilder = module.valueBuilderFactory().newValueBuilder( ContactValue.class );
            contactValueBuilder.prototype().contactId().set( contactId );

            ContactList contactList = contactLookup.lookup( contactValueBuilder.newInstance() );
            if( !contactList.contacts().get().isEmpty() )
            {
               ContactValue contactValue = contactList.contacts().get().get( 0 );
               customer.changeDescription( contactValue.name().get() );

               ContactBuilder contactBuilder = new ContactBuilder(((Contactable) customer).getContact(), module.valueBuilderFactory() );
               contactBuilder.contactId( contactValue.contactId().get() );
               contactBuilder.name( contactValue.name().get() );
               contactBuilder.address( contactValue.addresses().get().get( 0 ).address().get() );
               contactBuilder.zipCode( contactValue.addresses().get().get( 0 ).zipCode().get() );
               contactBuilder.city( contactValue.addresses().get().get( 0 ).city().get() );

               ((Contactable) customer).updateContact( contactBuilder.newInstance() );
               return customer;
            }
         }
         customer.changeDescription( name );
         ValueBuilder<ContactDTO> valueBuilder = module.valueBuilderFactory().newValueBuilder( ContactDTO.class ).withPrototype( ((Contactable) customer).getContact() );
         valueBuilder.prototype().contactId().set( contactId );
         valueBuilder.prototype().name().set( name );
         ((Contactable) customer).updateContact( valueBuilder.newInstance() );
         return customer;
      }

      public Customer createdCustomerById(DomainEvent event, String id)
      {
         EntityBuilder<Customer> builder = module.unitOfWorkFactory().currentUnitOfWork()
               .newEntityBuilder( Customer.class, id );
         Contactable.Data contacts = builder.instanceFor( Contactable.Data.class );
         ContactDTO contactDTO = module.valueBuilderFactory().newValue( ContactDTO.class );
         contacts.contact().set( contactDTO );
         return builder.newInstance();
      }
   }
}