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

package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolutions;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedResolutions;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

/**
 * JAVADOC
 */
public class SelectedResolutionsContext
   implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      SelectedResolutions.Data resolutions = RoleMap.role(SelectedResolutions.Data.class);

      return new LinksBuilder( module.valueBuilderFactory() ).rel( "resolution" ).addDescribables( resolutions.selectedResolutions() ).newLinks();
   }

   public LinksValue possibleresolutions()
   {
      OrganizationQueries organizationQueries = RoleMap.role(OrganizationQueries.class);
      final SelectedResolutions.Data selectedResolutions = RoleMap.role(SelectedResolutions.Data.class);

      final LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).command("addresolution");
      organizationQueries.visitOrganization(new OrganizationVisitor()
      {

         Describable owner;

         @Override
         public boolean visitOrganizationalUnit(OrganizationalUnit ou)
         {
            owner = ou;

            return super.visitOrganizationalUnit(ou);
         }

         @Override
         public boolean visitProject(Project project)
         {
            owner = project;

            return super.visitProject(project);
         }

         @Override
         public boolean visitCaseType(CaseType caseType)
         {
            owner = caseType;

            return super.visitCaseType(caseType);
         }

         @Override
         public boolean visitResolution(Resolution resolution)
         {
            if (!selectedResolutions.selectedResolutions().contains(resolution))
               builder.addDescribable(resolution, owner);

            return true;
         }
      }, new OrganizationQueries.ClassSpecification(
            OrganizationalUnits.class,
            OrganizationalUnit.class,
            Projects.class,
            CaseTypes.class,
            CaseType.class,
            Resolutions.class));
      return builder.newLinks();
   }

   public void addresolution( EntityValue resolutionDTO )
   {
      SelectedResolutions resolutions = RoleMap.role( SelectedResolutions.class);
      Resolution resolution = module.unitOfWorkFactory().currentUnitOfWork().get( Resolution.class, resolutionDTO.entity().get() );

      resolutions.addSelectedResolution( resolution );
   }
}