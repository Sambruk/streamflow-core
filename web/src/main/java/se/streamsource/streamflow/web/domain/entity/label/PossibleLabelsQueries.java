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

package se.streamsource.streamflow.web.domain.entity.label;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.Specification;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.Projects;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;

/**
 * JAVADOC
 */
@Mixins(PossibleLabelsQueries.Mixin.class)
public interface PossibleLabelsQueries
{
   void possibleLabels( LinksBuilder builder, Specification<Label> specification );

   class Mixin
      implements PossibleLabelsQueries
   {
      @This
      OrganizationalUnits.Data ous;

      @This
      Describable describable;

      @This
      Labels.Data labels;

      public void possibleLabels( LinksBuilder builder, Specification<Label> specification )
      {
         for (Label label : labels.labels())
         {
            if (specification.valid( label ))
               builder.addDescribable( label, describable );
         }

         for (OrganizationalUnit organizationalUnit : ous.organizationalUnits())
         {
            possibleLabelsforOU( builder, specification, organizationalUnit );
         }
      }

      private void possibleLabelsforOU( LinksBuilder builder, Specification<Label> specification, OrganizationalUnit organizationalUnit )
      {
         {
            Labels.Data labels = (Labels.Data) organizationalUnit;
            for (Label label : labels.labels())
            {
               if (specification.valid( label ))
                  builder.addDescribable( label, organizationalUnit );
            }
         }

         Projects.Data projects = (Projects.Data)organizationalUnit;
         for (Project project : projects.projects())
         {
            {
               Labels.Data labels = (Labels.Data) project;
               for (Label label : labels.labels())
               {
                  if (specification.valid( label ))
                     builder.addDescribable( label, project );
               }
            }

            CaseTypes.Data caseTypes = (CaseTypes.Data) project;
            for (CaseType caseType : caseTypes.caseTypes())
            {
               Labels.Data labels = (Labels.Data) caseType;
               for (Label label : labels.labels())
               {
                  if (specification.valid( label ))
                     builder.addDescribable( label, caseType );
               }
            }
         }

         // Sub-OU's
         for (OrganizationalUnit ou : ((OrganizationalUnits.Data)organizationalUnit).organizationalUnits())
         {
            possibleLabelsforOU( builder, specification, ou );
         }
      }
   }
}
