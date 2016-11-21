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
package se.streamsource.streamflow.web.rest.resource.surface.customers;

import org.qi4j.api.query.Query;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.surface.customers.CustomersContext;
import se.streamsource.streamflow.web.domain.entity.customer.CustomerEntity;
import se.streamsource.streamflow.web.domain.structure.customer.Customer;

/**
 * JAVADOC
 */
public class CustomersResource extends CommandQueryResource implements SubResources
{
   public CustomersResource()
   {
      super( CustomersContext.class );
   }

   public void resource(String segment) throws ResourceException
   {
      StringBuilder queryBuilder = new StringBuilder();
      queryBuilder.append( " type:se.streamsource.streamflow.web.domain.entity.customer.CustomerEntity" );
      queryBuilder.append( " contactId:" + segment );
      Query<Customer> query = module.queryBuilderFactory()
            .newNamedQuery( Customer.class, module.unitOfWorkFactory().currentUnitOfWork(), "solrquery" )
            .setVariable( "query", queryBuilder.toString() );
      if (query.iterator().hasNext())
      {
         RoleMap.current().set( query.iterator().next(), CustomerEntity.class );
         subResource( CustomerResource.class );
      } else
      {
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
      }
   }
}