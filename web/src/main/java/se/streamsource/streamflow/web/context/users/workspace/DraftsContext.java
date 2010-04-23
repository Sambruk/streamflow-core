/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.users.workspace;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.restlet.data.Reference;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.web.context.caze.CasesContext;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.entity.gtd.DraftsQueries;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Mixins(DraftsContext.Mixin.class)
public interface DraftsContext
      extends Interactions
{
   LinksValue cases();

   void createdraft();

   abstract class Mixin
      extends InteractionsMixin
      implements DraftsContext
   {
      public LinksValue cases( )
      {
         DraftsQueries inbox = context.get( DraftsQueries.class);

         QueryBuilder<Case> builder = inbox.drafts();
         Query<Case> query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).orderBy( orderBy( templateFor( CreatedOn.class ).createdOn() ) );

         return CasesContext.Mixin.buildCaseList(query, module, context.get( Reference.class).getBaseRef().getPath());
      }

      public void createdraft()
      {
         Drafts drafts = context.get( Drafts.class );
         drafts.createDraft();
      }
   }
}
