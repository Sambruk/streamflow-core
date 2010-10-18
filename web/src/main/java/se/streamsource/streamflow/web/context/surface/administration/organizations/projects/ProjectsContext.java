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

package se.streamsource.streamflow.web.context.surface.administration.organizations.projects;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

/**
 * JAVADOC
 */
@Mixins(ProjectsContext.Mixin.class)
public interface ProjectsContext
   extends SubContexts<CaseTypesContext>, IndexContext<LinksValue>, Context
{
   abstract class Mixin
      extends ContextMixin
      implements ProjectsContext
   {
      public LinksValue index()
      {
         final LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );
         OrganizationQueries organizationQueries = roleMap.get( OrganizationQueries.class );
         organizationQueries.visitOrganization( new OrganizationVisitor()
         {
            @Override
            public boolean visitProject( Project project )
            {
               linksBuilder.addDescribable( project );

               return true;
            }
         }, new OrganizationQueries.ClassSpecification( OrganizationalUnits.class, Projects.class, Project.class));

         return linksBuilder.newLinks();
      }

      public CaseTypesContext context( String id )
      {
         roleMap.set( module.unitOfWorkFactory().currentUnitOfWork().get( Project.class, id ) );

         return subContext( CaseTypesContext.class);
      }
   }
}