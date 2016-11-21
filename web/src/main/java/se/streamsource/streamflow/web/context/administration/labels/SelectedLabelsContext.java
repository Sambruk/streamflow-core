/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.context.administration.labels;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationVisitor;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

/**
 * JAVADOC
 */
public class SelectedLabelsContext
   implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      SelectedLabels.Data labels = role(SelectedLabels.Data.class);

      return new LinksBuilder( module.valueBuilderFactory() ).rel( "label" ).addDescribables( labels.selectedLabels() ).newLinks();
   }

   public LinksValue possiblelabels()
   {
      OrganizationQueries orgQueries = role(OrganizationQueries.class);
      final SelectedLabels.Data selectedLabels = role(SelectedLabels.Data.class);

      final LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()).command( "addlabel" );

      orgQueries.visitOrganization( new OrganizationVisitor()
      {

         Describable owner;

         @Override
         public boolean visitOrganization( Organization org )
         {
            owner = org;
            return super.visitOrganization( org );
         }

         @Override
         public boolean visitOrganizationalUnit( OrganizationalUnit ou )
         {
            owner = ou;

            return super.visitOrganizationalUnit( ou );
         }

         @Override
         public boolean visitProject( Project project )
         {
            owner = project;

            return super.visitProject( project );
         }

         @Override
         public boolean visitCaseType( CaseType caseType )
         {
            owner = caseType;

            return super.visitCaseType( caseType );
         }

         @Override
         public boolean visitLabel( Label label )
         {
            if (!selectedLabels.selectedLabels().contains( label ))
               builder.addDescribable( label, owner );

            return true;
         }
      }, new OrganizationQueries.ClassSpecification( Organization.class,
            OrganizationalUnits.class,
            OrganizationalUnit.class,
            Projects.class,
            Project.class,
            CaseTypes.class,
            CaseType.class,
            Labels.class));
      
      return builder.newLinks();
   }

   public void createlabel( @Name("name") String name )
   {
      Labels labels = role(Labels.class);
      SelectedLabels selectedLabels = role(SelectedLabels.class);

      Label label = labels.createLabel( name );
      selectedLabels.addSelectedLabel( label );
   }

   public void addlabel( @Name("entity") Label label)
   {
      SelectedLabels labels = role( SelectedLabels.class);
      labels.addSelectedLabel( label );
   }
}
