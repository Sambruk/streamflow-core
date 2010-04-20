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

package se.streamsource.streamflow.web.context.users.overview;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.restlet.data.Reference;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.web.context.caze.CasesContext;
import se.streamsource.streamflow.web.domain.entity.gtd.WaitingForQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatable;
import se.streamsource.dci.api.Interactions;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Mixins(OverviewProjectWaitingForContext.Mixin.class)
public interface OverviewProjectWaitingForContext
   extends Interactions
{
   public LinksValue cases();

   abstract class Mixin
      extends InteractionsMixin
      implements OverviewProjectWaitingForContext
   {
      public LinksValue cases()
      {
         WaitingForQueries waitingForQueries = context.get( WaitingForQueries.class);

         QueryBuilder<Delegatable> builder = waitingForQueries.waitingFor( null );
         Query query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         query = query.orderBy( orderBy( templateFor( Delegatable.Data.class ).delegatedOn() ) );
         return CasesContext.Mixin.buildCaseList(query, module, context.get( Reference.class).getBaseRef().getPath());
      }

   }
}