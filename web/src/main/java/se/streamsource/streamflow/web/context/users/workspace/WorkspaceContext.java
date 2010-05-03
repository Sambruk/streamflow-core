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

package se.streamsource.streamflow.web.context.users.workspace;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContext;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.DraftsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.InboxQueries;
import se.streamsource.streamflow.web.domain.entity.user.ProjectQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.structure.project.Project;

/**
 * JAVADOC
 */
@Mixins(WorkspaceContext.Mixin.class)
public interface WorkspaceContext
   extends Interactions
{
   LinksValue casecounts();

   @SubContext
   WorkspaceUserContext user();

   @SubContext
   WorkspaceProjectsContext projects();

   @SubContext
   SavedSearchesContext savedsearches();

   abstract class Mixin
      extends InteractionsMixin
      implements WorkspaceContext
   {
      @Structure
      Module module;

      public LinksValue casecounts()
      {
         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());

         UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

         builder.addLink( context.get( DraftsQueries.class ).drafts().newQuery( uow ).count()+"", "drafts" );

         for (Project project : context.get( ProjectQueries.class ).allProjects())
         {
            builder.addLink( ((InboxQueries)project).inbox().newQuery( uow ).count()+"", project+"/inbox" );
            builder.addLink( ((AssignmentsQueries)project).assignments( context.get( Assignee.class) ).newQuery( uow ).count()+"", project+"/assignments" );
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

      public SavedSearchesContext savedsearches()
      {
         return subContext( SavedSearchesContext.class );
      }
   }

}
