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
package se.streamsource.streamflow.web.context.surface.customers;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.surface.api.CaseListItemDTO;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.customer.Customer;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

/**
 * JAVADOC
 */
public class OpenCasesContext 
{
   @Structure
   Module module;

   public LinksValue cases()
   {
      Customer customer = RoleMap.role( Customer.class );

      StringBuilder queryBuilder = new StringBuilder();
      queryBuilder.append( " type:se.streamsource.streamflow.web.domain.entity.caze.CaseEntity" );
      queryBuilder.append( " status:OPEN" );
      queryBuilder.append( " contactId:" + ((Contactable)customer).getContact().contactId().get());
      
      Query<Case> query = module.queryBuilderFactory()
            .newNamedQuery( Case.class, module.unitOfWorkFactory().currentUnitOfWork(), "solrquery" )
            .setVariable( "query", queryBuilder.toString() );

      Iterable<Case> cases = Iterables.filter( new Specification<Case>()
      {
         public boolean satisfiedBy(Case item)
         {
            return !((Removable.Data) item).removed().get();
         }
      }, query );
      
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      for (Case caze : cases)
      {
         ValueBuilder<CaseListItemDTO> valueBuilder = module.valueBuilderFactory().newValueBuilder( CaseListItemDTO.class );
         valueBuilder.prototype().rel().set( "mycases/opencase" );
         valueBuilder.prototype().href().set( EntityReference.getEntityReference( caze ).identity() + "/" );
         valueBuilder.prototype().id().set( EntityReference.getEntityReference( caze ).identity() );
         valueBuilder.prototype().caseId().set( ((CaseId.Data)caze).caseId().get() );
         valueBuilder.prototype().text().set( caze.getDescription() );
         valueBuilder.prototype().creationDate().set( caze.createdOn().get() );
         valueBuilder.prototype().caseType().set( ((TypedCase.Data)caze).caseType().get() != null ? ((TypedCase.Data)caze).caseType().get().getDescription() : "" );
         valueBuilder.prototype().project().set( ((Describable)((Ownable.Data)caze).owner().get()).getDescription() );
         
         builder.addLink( valueBuilder.newInstance() );
      }
      return builder.newLinks();
   }

}