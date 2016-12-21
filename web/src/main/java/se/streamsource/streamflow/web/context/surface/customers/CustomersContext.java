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
package se.streamsource.streamflow.web.context.surface.customers;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.web.domain.entity.customer.CustomersEntity;
import se.streamsource.streamflow.web.domain.structure.customer.Customer;
import se.streamsource.streamflow.web.domain.structure.customer.Customers;

/**
 * JAVADOC
 */
public class CustomersContext
{

   @Structure
   Module module;

   public Customer create(@Name("contactid") String contactId, @Name("name") String name)
   {
      Customers customers = module.unitOfWorkFactory().currentUnitOfWork()
            .get( Customers.class, CustomersEntity.CUSTOMERS_ID );
      Customer customer = customers.createCustomerById( contactId, name  );
      return customer;
   }
}