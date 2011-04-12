/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.context.workspace;

import net.sf.ehcache.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.unitofwork.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.gtd.*;
import se.streamsource.streamflow.web.domain.entity.user.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;
import se.streamsource.streamflow.web.domain.structure.project.*;
import se.streamsource.streamflow.web.domain.structure.user.*;
import se.streamsource.streamflow.web.infrastructure.caching.*;

/**
 * JAVADOC
 */
public class WorkspaceContext
        implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   @Service
   CachingService caching;

   public LinksValue index()
   {
      LinksBuilder linksBuilder = new LinksBuilder(module.valueBuilderFactory());
      ProjectQueries projectQueries = RoleMap.role(ProjectQueries.class);
      Perspectives.Data perspectives = RoleMap.role(Perspectives.Data.class);

      linksBuilder.addLink("Drafts", "drafts", "drafts", "drafts/", "drafts");
      linksBuilder.addLink("Search", "search", "search", "search/", "search");

      for (Perspective perspective : perspectives.perspectives())
      {
         linksBuilder.addLink(perspective.getDescription(), perspective.toString(),
                 "perspective", "perspectives/" + perspective.toString() + "/", "perspective");
      }

      for (Project project : projectQueries.allProjects())
      {
         linksBuilder.addLink(project.getDescription(), project.toString(), "inbox", "projects/" + project.toString() + "/inbox/", "inbox");
         linksBuilder.addLink(project.getDescription(), project.toString(), "assignments", "projects/" + project.toString() + "/assignments/", "assignments");
      }

      return linksBuilder.newLinks();
   }

   /**
    * Calculate casecounts for this user. Uses caching if available.
    *
    * @return
    */
   public LinksValue casecounts()
   {
      Caching caching = new Caching(this.caching, Caches.CASECOUNTS);

      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());

      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

      Element caseCount;
      DraftsQueries drafts = RoleMap.role(DraftsQueries.class);
      if ((caseCount = caching.get(drafts.toString())) == null)
      {
         caseCount = new Element(drafts.toString(), Long.toString(drafts.drafts(null).newQuery(uow).count()));
         caching.put(caseCount);
      }
      builder.addLink((String) caseCount.getObjectValue(), "/drafts");

      for (Project project : RoleMap.role(ProjectQueries.class).allProjects())
      {
         if ((caseCount = caching.get(project.toString())) == null)
         {
            caseCount = new Element(project.toString(), Long.toString(((InboxQueries) project).inbox(null).newQuery(uow).count()));
            caching.put(caseCount);
         }

         builder.addLink((String) caseCount.getObjectValue(), project + "/inbox");

         if ((caseCount = caching.get(project.toString() + ":" + RoleMap.role(Assignee.class).toString())) == null)
         {
            caseCount = new Element(project.toString() + ":" + RoleMap.role(Assignee.class).toString(),
                    Long.toString(((AssignmentsQueries) project).assignments(RoleMap.role(Assignee.class), null).newQuery(uow).count()));
            caching.put(caseCount);
         }

         builder.addLink((String) caseCount.getObjectValue(), project + "/assignments");
      }

      return builder.newLinks();
   }
}
