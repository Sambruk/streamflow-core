/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.access.organizations;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.gtd.Inbox;
import se.streamsource.streamflow.web.domain.entity.gtd.InboxQueries;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;

/**
 * JAVADOC
 */
@Mixins(ProxyUserContext.Mixin.class)
public interface ProxyUserContext
   extends SubContexts<TaskContext>, Context, IndexContext<LinksValue>
{
   // command
   void createtask( StringValue description );


   abstract class Mixin
      extends ContextMixin
      implements ProxyUserContext
   {

      public LinksValue index()
      {
         InboxQueries inboxQueries = context.role( InboxQueries.class );

         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         linksBuilder.addDescribables( inboxQueries.inbox().newQuery( module.unitOfWorkFactory().currentUnitOfWork() ));

         return linksBuilder.newLinks();
      }

      public void createtask( StringValue description )
      {
         Inbox inbox = context.role( Inbox.class );
         AccessPoint.Data data = context.role( AccessPoint.Data.class );
         TaskEntity taskEntity = inbox.createTask();
         taskEntity.changeDescription( description.string().get() );
         taskEntity.changeTaskType( data.taskType().get() );
         for (Label label : data.labels().get())
         {
            taskEntity.addLabel( label );
         }
      }

      public TaskContext context( String id)
      {
         TaskEntity taskEntity = module.unitOfWorkFactory().currentUnitOfWork().get( TaskEntity.class, id );

         context.playRoles( taskEntity );
         return subContext( TaskContext.class );
      }
   }
}