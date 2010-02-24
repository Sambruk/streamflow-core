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

package se.streamsource.streamflow.web.context.gtd;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.web.context.task.TasksContext;
import se.streamsource.streamflow.web.domain.entity.gtd.WaitingForQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegator;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Mixins(WaitingForContext.Mixin.class)
public interface WaitingForContext
   extends Context
{
   LinksValue tasks();

   abstract class Mixin
      extends ContextMixin
      implements WaitingForContext
   {
      public LinksValue tasks( )
      {
         WaitingForQueries waitingForQueries = context.role( WaitingForQueries.class);

         QueryBuilder<Delegatable> builder = waitingForQueries.waitingFor( context.role( Delegator.class ));
         Query query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         query = query.orderBy( orderBy( templateFor( Delegatable.Data.class ).delegatedOn() ) );
         return TasksContext.Mixin.buildTaskList(query, module);
      }
   }
}