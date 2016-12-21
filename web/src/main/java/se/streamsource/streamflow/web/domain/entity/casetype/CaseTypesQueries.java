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
package se.streamsource.streamflow.web.domain.entity.casetype;

import static org.qi4j.api.query.QueryExpressions.and;
import static org.qi4j.api.query.QueryExpressions.contains;
import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.isNotNull;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

/**
 * JAVADOC
 */
@Mixins(CaseTypesQueries.Mixin.class)
public interface CaseTypesQueries
{
   // Queries
   QueryBuilder<Project> possibleProjects( @Optional CaseType caseType );

   void caseTypes( LinksBuilder builder, Specification<CaseType> specification );

    void removedCaseTypes( LinksBuilder builder );

    abstract class Mixin
         implements CaseTypesQueries, CaseTypes.Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      Module module;

      @This
      Describable describable;

      @This
      OrganizationalUnits.Data ous;

      public void caseTypes( LinksBuilder builder, Specification<CaseType> specification )
      {
         for (CaseType caseType : caseTypes())
         {
            builder.addDescribable( caseType, describable );
         }

         for (OrganizationalUnit organizationalUnit : ous.organizationalUnits())
         {
            caseTypesforOU( builder, specification, organizationalUnit );
         }
      }

      private void caseTypesforOU( LinksBuilder builder, Specification<CaseType> specification, OrganizationalUnit organizationalUnit )
      {
         {
            CaseTypes.Data caseTypes = (CaseTypes.Data) organizationalUnit;
            for (CaseType caseType : caseTypes.caseTypes())
            {
               if (specification.satisfiedBy( caseType ))
                  builder.addDescribable( caseType, organizationalUnit );
            }
         }

         Projects.Data projects = (Projects.Data)organizationalUnit;
         for (Project project : projects.projects())
         {
            CaseTypes.Data caseTypes = (CaseTypes.Data) project;
            for (CaseType caseType : caseTypes.caseTypes())
            {
               if (specification.satisfiedBy( caseType ))
                  builder.addDescribable( caseType, project );
            }
         }

         // Sub-OU's
         for (OrganizationalUnit ou : ((OrganizationalUnits.Data)organizationalUnit).organizationalUnits())
         {
            caseTypesforOU( builder, specification, ou );
         }
      }

      public QueryBuilder<Project> possibleProjects( CaseType caseType )
      {
         QueryBuilder<Project> projects = module.queryBuilderFactory().newQueryBuilder(Project.class);

         SelectedCaseTypes.Data template = templateFor( SelectedCaseTypes.Data.class );

         if (caseType != null)
            projects = projects.where( and( contains( template.selectedCaseTypes(), caseType ),eq( templateFor( Removable.Data.class ).removed(), false ) ) );
         else
         {
            projects = projects.where( and( eq( templateFor( Removable.Data.class ).removed(), false ),
                                            isNotNull( templateFor(OwningOrganizationalUnit.Data.class).organizationalUnit() )));
         }

         return projects;
      }

      public void removedCaseTypes( LinksBuilder linksBuilder )
      {
          Removable.Data template = templateFor(Removable.Data.class);
          QueryBuilder<CaseType> builder = module.queryBuilderFactory().newQueryBuilder(CaseType.class);

          Query<CaseType> removedCaseTypes = builder.where( eq(template.removed(), true ) ).newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

          for( CaseType caseType : removedCaseTypes )
          {
              if( caseType.hasOwner() )
              {
                  Owner owner = ((Ownable.Data)caseType).owner().get();
                  linksBuilder.addDescribable( caseType, (Describable)owner );
              } else
              {
                  linksBuilder.addDescribable( caseType, "" );
              }
          }
      }
   }
}