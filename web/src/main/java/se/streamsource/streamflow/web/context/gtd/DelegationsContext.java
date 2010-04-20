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

package se.streamsource.streamflow.web.context.gtd;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.restlet.data.Reference;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.web.context.task.TasksContext;
import se.streamsource.streamflow.web.domain.entity.gtd.DelegationsQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatable;
import se.streamsource.dci.api.InteractionsMixin;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Mixins(DelegationsContext.Mixin.class)
public interface DelegationsContext
   extends Interactions
{
   LinksValue tasks();

   abstract class Mixin
      extends InteractionsMixin
      implements DelegationsContext
   {
      public LinksValue tasks( )
      {
         DelegationsQueries delegations = context.get( DelegationsQueries.class);
         QueryBuilder<Delegatable> builder = delegations.delegations();
         Query query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).orderBy( orderBy( templateFor( Delegatable.Data.class ).delegatedOn() ) );

         return TasksContext.Mixin.buildTaskList( query, module, context.get( Reference.class).getBaseRef().getPath() );
      }
   }
}