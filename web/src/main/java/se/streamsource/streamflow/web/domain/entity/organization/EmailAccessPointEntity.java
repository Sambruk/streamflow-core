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
package se.streamsource.streamflow.web.domain.entity.organization;

import java.util.List;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.This;

import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.IdGenerator;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPointSettings;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoint;
import se.streamsource.streamflow.web.domain.structure.project.Project;

/**
 * An Email Access Point
 */
@Concerns({EmailAccessPointEntity.AddProjectConcern.class,
      EmailAccessPointEntity.AddCaseTypeConcern.class,
      EmailAccessPointEntity.RemoveCaseTypeConcern.class})
public interface EmailAccessPointEntity
      extends DomainEntity,
        EmailAccessPoint,

      // Interactions
      IdGenerator,

      // Data
      Describable.Data,
      IdGenerator.Data,
      AccessPointSettings.Data,
      Labelable.Data,
      Removable.Data
{
   abstract class AddProjectConcern
         extends ConcernOf<AccessPoint>
         implements AccessPoint
   {
      public void changedProject(Project project)
      {
         next.changedProject(project);
         removeCaseType();
      }
   }

   abstract class AddCaseTypeConcern
         extends ConcernOf<EmailAccessPoint>
         implements EmailAccessPoint
   {
      @This
      Labelable.Data labels;

      public void changedCaseType(CaseType caseType)
      {
         next.changedCaseType(caseType);
         List<Label> labelList = labels.labels().toList();
         for (Label label : labelList)
         {
            removeLabel( label );
         }
      }
   }

   abstract class RemoveCaseTypeConcern
         extends ConcernOf<EmailAccessPoint>
         implements EmailAccessPoint
   {
      @This
      Labelable.Data labels;

      public void removeCaseType()
      {
         next.removeCaseType();
         List<Label> labelList = labels.labels().toList();
         for (Label label : labelList)
         {
            removeLabel(label);
         }
      }
   }

}