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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.SubContext;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.DelegationsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.InboxQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.WaitingForQueries;
import se.streamsource.streamflow.web.domain.entity.user.ProjectQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegator;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.User;

/**
 * JAVADOC
 */
@Mixins(WorkspaceContext.Mixin.class)
public interface WorkspaceContext
   extends Context
{
   LinksValue taskcounts();

   @SubContext
   WorkspaceUserContext user();

   @SubContext
   WorkspaceProjectsContext projects();

   abstract class Mixin
      extends ContextMixin
      implements WorkspaceContext
   {
      @Structure
      Module module;

      public LinksValue taskcounts()
      {
         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());

         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

         String user = context.role( User.class ).toString();
         builder.addLink( context.role( InboxQueries.class ).inbox().newQuery( uow ).count()+"", "inbox" );
         builder.addLink( context.role( AssignmentsQueries.class ).assignments( context.role( Assignee.class) ).newQuery( uow ).count()+"", "assignments" );
         builder.addLink( context.role( DelegationsQueries.class ).delegations().newQuery( uow ).count()+"", "delegations" );
         builder.addLink( context.role( WaitingForQueries.class ).waitingFor(context.role( Delegator.class)).newQuery( uow ).count()+"", "waitingfor" );

         for (Project project : context.role( ProjectQueries.class ).allProjects())
         {
            builder.addLink( ((InboxQueries)project).inbox().newQuery( uow ).count()+"", project+"/inbox" );
            builder.addLink( ((AssignmentsQueries)project).assignments( context.role( Assignee.class) ).newQuery( uow ).count()+"", project+"/assignments" );
            builder.addLink( (( DelegationsQueries)project).delegations().newQuery( uow ).count()+"", project+"/delegations" );
            builder.addLink( (( WaitingForQueries )project).waitingFor(context.role( Delegator.class)).newQuery( uow ).count()+"", project+"/waitingfor" );
         }

         return builder.newLinks();
      }

      public WorkspaceUserContext user()
      {
         return subContext( WorkspaceUserContext.class );
      }

      public WorkspaceProjectsContext projects()
      {
         return subContext( WorkspaceProjectsContext.class );
      }
   }

}
