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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Reference;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.web.context.task.TasksContext;
import se.streamsource.streamflow.web.domain.entity.gtd.Inbox;
import se.streamsource.streamflow.web.domain.entity.gtd.InboxQueries;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.task.Task;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;

import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

/**
 * JAVADOC
 */
@Mixins(InboxContext.Mixin.class)
public interface InboxContext
   extends Context
{
   LinksValue tasks();

   void createtask();

   abstract class Mixin
      extends ContextMixin
      implements InboxContext
   {
      @Structure
      ValueBuilderFactory vbf;

      public LinksValue tasks( )
      {
         InboxQueries inbox = context.role( InboxQueries.class);

         QueryBuilder<Task> builder = inbox.inbox();
         Query<Task> query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).orderBy( orderBy( templateFor( CreatedOn.class ).createdOn() ) );
         
         return TasksContext.Mixin.buildTaskList(query, module, context.role( Reference.class).getBaseRef().getPath());
      }

      public void createtask()
      {
         Inbox inbox = context.role( Inbox.class );
         inbox.createTask();
      }
   }
}
