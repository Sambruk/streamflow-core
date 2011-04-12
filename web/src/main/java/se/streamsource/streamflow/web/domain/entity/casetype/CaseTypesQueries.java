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

package se.streamsource.streamflow.web.domain.entity.casetype;

import org.qi4j.api.common.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.query.*;
import org.qi4j.api.specification.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.value.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.infrastructure.application.*;
import se.streamsource.streamflow.web.domain.structure.casetype.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.project.*;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
@Mixins(CaseTypesQueries.Mixin.class)
public interface CaseTypesQueries
{
   // Queries
   QueryBuilder<Project> possibleProjects( @Optional CaseType caseType );

   void caseTypes( LinksBuilder builder, Specification<CaseType> specification );

   abstract class Mixin
         implements CaseTypesQueries, CaseTypes.Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

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
         QueryBuilder<Project> projects = qbf.newQueryBuilder( Project.class );

         SelectedCaseTypes.Data template = templateFor( SelectedCaseTypes.Data.class );

         if (caseType != null)
            projects = projects.where( contains( template.selectedCaseTypes(), caseType ) );
         else
         {
            projects = projects.where( and( eq( templateFor( Removable.Data.class ).removed(), false ),
                                            isNotNull( templateFor(OwningOrganizationalUnit.Data.class).organizationalUnit() )));
         }

         return projects;
      }
   }
}