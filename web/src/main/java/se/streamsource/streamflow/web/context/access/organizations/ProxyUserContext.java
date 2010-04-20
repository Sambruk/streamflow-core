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

package se.streamsource.streamflow.web.context.access.organizations;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.IndexInteraction;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.TitledLinksBuilder;
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
   extends SubContexts<CaseContext>, Interactions, IndexInteraction<LinksValue>
{
   // command
   void createcase( StringValue description );


   abstract class Mixin
      extends InteractionsMixin
      implements ProxyUserContext
   {

      public LinksValue index()
      {
         InboxQueries inboxQueries = context.get( InboxQueries.class );
         Describable describable = context.get( Describable.class );

         TitledLinksBuilder linksBuilder = new TitledLinksBuilder( module.valueBuilderFactory() );

         linksBuilder.addDescribables( inboxQueries.inbox().newQuery( module.unitOfWorkFactory().currentUnitOfWork() ));
         linksBuilder.addTitle( describable.getDescription() );

         return linksBuilder.newLinks();
      }

      public void createcase( StringValue description )
      {
         Inbox inbox = context.get( Inbox.class );
         AccessPoint.Data data = context.get( AccessPoint.Data.class );
         TaskEntity taskEntity = inbox.createTask();
         taskEntity.changeDescription( description.string().get() );
         taskEntity.changeTaskType( data.taskType().get() );
         for (Label label : data.labels().get())
         {
            taskEntity.addLabel( label );
         }
      }

      public CaseContext context( String id)
      {
         TaskEntity taskEntity = module.unitOfWorkFactory().currentUnitOfWork().get( TaskEntity.class, id );

         context.set( taskEntity );
         return subContext( CaseContext.class );
      }
   }
}