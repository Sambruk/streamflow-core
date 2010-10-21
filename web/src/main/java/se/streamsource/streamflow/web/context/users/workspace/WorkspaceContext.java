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

import net.sf.ehcache.Element;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.DraftsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.InboxQueries;
import se.streamsource.streamflow.web.domain.entity.user.ProjectQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

/**
 * JAVADOC
 */
public class WorkspaceContext
{
   @Structure
   Module module;

   @Service
   CachingService caching;

   /**
    * Calculate casecounts for this user. Uses caching if available.
    *
    * @return
    */
   public LinksValue casecounts()
   {
      Caching caching = new Caching( this.caching, Caches.CASECOUNTS );

      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );

      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

      Element caseCount;
      DraftsQueries drafts = RoleMap.role( DraftsQueries.class );
      if ((caseCount = caching.get( drafts.toString() )) == null)
      {
         caseCount = new Element( drafts.toString(), Long.toString( drafts.drafts().newQuery( uow ).count() ) );
         caching.put( caseCount );
      }
      builder.addLink( (String) caseCount.getObjectValue(), "user/drafts" );

      for (Project project : RoleMap.role( ProjectQueries.class ).allProjects())
      {
         if ((caseCount = caching.get( project.toString() )) == null)
         {
            caseCount = new Element( project.toString(), Long.toString( ((InboxQueries) project).inbox().newQuery( uow ).count() ) );
            caching.put( caseCount );
         }

         builder.addLink( (String) caseCount.getObjectValue(), project + "/inbox" );

         if ((caseCount = caching.get( project.toString() + ":" + RoleMap.role( Assignee.class ).toString() )) == null)
         {
            caseCount = new Element( project.toString() + ":" + RoleMap.role( Assignee.class ).toString(), Long.toString( ((AssignmentsQueries) project).assignments( RoleMap.role( Assignee.class ) ).count() ) );
            caching.put( caseCount );
         }

         builder.addLink( (String) caseCount.getObjectValue(), project + "/assignments" );
      }

      return builder.newLinks();
   }
}
