/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import net.sf.ehcache.Element;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.ProjectListValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.gtd.DraftsQueries;
import se.streamsource.streamflow.web.domain.entity.user.ProjectQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.Perspective;
import se.streamsource.streamflow.web.domain.structure.user.Perspectives;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;
import se.streamsource.streamflow.web.infrastructure.caching.CaseCountItem;

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

      Caching caching = new Caching( this.caching, Caches.CASECOUNTS );
      LinksBuilder linksBuilder = new LinksBuilder(module.valueBuilderFactory());
      ProjectQueries projectQueries = RoleMap.role(ProjectQueries.class);
      Perspectives.Data perspectives = RoleMap.role(Perspectives.Data.class);
      ValueBuilder<ProjectListValue> projectListBuilder = module.valueBuilderFactory().newValueBuilder( ProjectListValue.class );
      
      projectListBuilder.prototype().id().set( "drafts" );
      projectListBuilder.prototype().href().set("drafts/");
      projectListBuilder.prototype().rel().set("drafts");
      projectListBuilder.prototype().classes().set("drafts");
      projectListBuilder.prototype().text().set( "Drafts" );
      projectListBuilder.prototype().caseCount().set(draftsCaseCounts());
      projectListBuilder.prototype().unreadCaseCount().set(0);
      linksBuilder.addLink( projectListBuilder.newInstance() );
      
      linksBuilder.addLink("Search", "search", "search", "search/", "search");

      for (Perspective perspective : perspectives.perspectives())
      {
         linksBuilder.addLink(perspective.getDescription(), perspective.toString(),
                 "perspective", "perspectives/" + perspective.toString() + "/", "perspective");
      }

      for (Project project : projectQueries.allProjects())
      {
         projectListBuilder.prototype().id().set( project.toString() );
         projectListBuilder.prototype().href().set("projects/" + project.toString() + "/inbox/");
         projectListBuilder.prototype().rel().set("inbox");
         projectListBuilder.prototype().classes().set("inbox");
         projectListBuilder.prototype().text().set( project.getDescription() );
         Element element = caching.get( project.toString() );
         projectListBuilder.prototype().caseCount().set(element != null ? ((CaseCountItem)element.getObjectValue()).getCount() : 0);
         projectListBuilder.prototype().unreadCaseCount().set(element != null ? ((CaseCountItem)element.getObjectValue()).getUnread() : 0);
         linksBuilder.addLink( projectListBuilder.newInstance() );

         projectListBuilder.prototype().href().set("projects/" + project.toString() + "/assignments/");
         projectListBuilder.prototype().rel().set("assignments");
         projectListBuilder.prototype().classes().set("assignments");

         element = caching.get( project.toString() + ":" + RoleMap.role(Assignee.class).toString());
         projectListBuilder.prototype().caseCount().set(element != null ? ((CaseCountItem)element.getObjectValue()).getCount() : 0);
         projectListBuilder.prototype().unreadCaseCount().set(element != null ? ((CaseCountItem)element.getObjectValue()).getUnread() : 0);
         linksBuilder.addLink( projectListBuilder.newInstance() );
         
      }

      return linksBuilder.newLinks();
   }

   private Integer draftsCaseCounts() {
      Caching caching = new Caching(this.caching, Caches.CASECOUNTS);
      
      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

      Element caseCount;
      DraftsQueries drafts = RoleMap.role(DraftsQueries.class);
      if ((caseCount = caching.get(drafts.toString())) == null)
      {
         caseCount = new Element(drafts.toString(), Long.toString(drafts.drafts(null).newQuery(uow).count()));
         caching.put(caseCount);
      }
      return Integer.parseInt( (String) caseCount.getObjectValue());
   }

   
   /**
    * Calculate casecounts for this user. Uses caching if available.
    *
    * @return
    */
   public LinksValue casecounts()
   {

      Caching caching = new Caching( this.caching, Caches.CASECOUNTS );
      
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());

      builder.addLink(draftsCaseCounts().toString(), "/drafts");

      for (Project project : RoleMap.role(ProjectQueries.class).allProjects())
      {
         Element element = caching.get( project.toString() );
         builder.addLink( (element != null ? Integer.toString(((CaseCountItem) element.getObjectValue()).getCount()) : "0" ), project + "/inbox");

         element = caching.get( project.toString() + ":" + RoleMap.role(Assignee.class).toString());
         builder.addLink( (element != null ? Integer.toString(((CaseCountItem) element.getObjectValue()).getCount()) : "0" ), project + "/assignments");
      }

      return builder.newLinks();
   }
}
