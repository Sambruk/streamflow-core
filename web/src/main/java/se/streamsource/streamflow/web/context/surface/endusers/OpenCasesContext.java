/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.context.surface.endusers;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.caze.CaseFormDTO;
import se.streamsource.streamflow.web.domain.entity.gtd.DraftsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.InboxQueries;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.form.EndUserCases;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.user.EndUser;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
public class OpenCasesContext
{
   @Structure
   Module module;

   public Query<Case> cases(TableQuery tableQuery)
   {
      EndUser endUser = RoleMap.role(EndUser.class);

      StringBuilder queryBuilder = new StringBuilder();
      queryBuilder.append( " type:se.streamsource.streamflow.web.domain.entity.caze.CaseEntity" );
      queryBuilder.append( " status:OPEN" );
      queryBuilder.append( " contactId:"+endUser.toString().split("/")[1]);
      Query<Case> query = module.queryBuilderFactory()
            .newNamedQuery( Case.class, module.unitOfWorkFactory().currentUnitOfWork(), "solrquery" ).setVariable( "query", queryBuilder.toString() );

      // Paging
      if (tableQuery.offset() != null)
         query.firstResult(Integer.parseInt(tableQuery.offset()));
      if (tableQuery.limit() != null)
         query.maxResults(Integer.parseInt(tableQuery.limit()));

      return query;
   }
}