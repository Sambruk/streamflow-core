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

package se.streamsource.streamflow.web.context.users.overview;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.web.context.task.TasksContext;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Mixins(OverviewProjectAssignmentsContext.Mixin.class)
public interface OverviewProjectAssignmentsContext
   extends Context
{
   public LinksValue tasks();

   abstract class Mixin
      extends ContextMixin
      implements OverviewProjectAssignmentsContext
   {
      public LinksValue tasks()
      {
         AssignmentsQueries assignmentsQueries = context.role(AssignmentsQueries.class);

         QueryBuilder<Assignable> builder = assignmentsQueries.assignments( null );
         Query query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).orderBy( orderBy( templateFor( CreatedOn.class ).createdOn() ) );
         return TasksContext.Mixin.buildTaskList( query, module);
      }

   }
}