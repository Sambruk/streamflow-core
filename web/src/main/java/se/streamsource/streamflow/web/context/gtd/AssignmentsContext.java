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

package se.streamsource.streamflow.web.context.gtd;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.restlet.data.Reference;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.web.context.caze.CasesContext;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountAssignmentsConcern.class)
@Mixins(AssignmentsContext.Mixin.class)
public interface AssignmentsContext
   extends Context
{
   LinksValue cases();

   public void createcase();

   abstract class Mixin
      extends ContextMixin
      implements AssignmentsContext
   {
      public LinksValue cases( )
      {
         AssignmentsQueries assignments = roleMap.get( AssignmentsQueries.class);

         QueryBuilder<Assignable> builder = assignments.assignments( roleMap.get( Assignee.class ) );
         Query query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).orderBy( orderBy( templateFor( CreatedOn.class ).createdOn() ) );
         return CasesContext.Mixin.buildCaseList( query, module, roleMap.get( Reference.class).getBaseRef().getPath());
      }

      public void createcase()
      {
         Drafts drafts = roleMap.get( Drafts.class );
         CaseEntity caze = drafts.createDraft();

         Owner owner = roleMap.get( Owner.class);
         caze.changeOwner( owner );

         caze.open();

         caze.assignTo( roleMap.get(Assignee.class) );
      }
   }
}