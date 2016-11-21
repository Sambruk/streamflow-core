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
import se.streamsource.streamflow.web.domain.structure.attachment.FormPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.MailSelectionMessage;
import se.streamsource.streamflow.web.domain.structure.form.RequiredSignatures;
import se.streamsource.streamflow.web.domain.structure.form.SelectedForms;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPointSettings;
import se.streamsource.streamflow.web.domain.structure.organization.WebAPReplacedSelectionFieldValues;
import se.streamsource.streamflow.web.domain.structure.project.Project;

/**
 * an Access Point
 */
@Concerns({AccessPointEntity.AddProjectConcern.class,
      AccessPointEntity.AddCaseTypeConcern.class,
      AccessPointEntity.RemoveCaseTypeConcern.class})
public interface AccessPointEntity
      extends DomainEntity,
      AccessPoint,

      // Interactions
      IdGenerator,

      // Data
      Describable.Data,
      IdGenerator.Data,
      AccessPointSettings.Data,
      AccessPointSettings.Events,
      Labelable.Data,
      SelectedForms.Data,
      FormPdfTemplate.Data,
      MailSelectionMessage.Data,
      Removable.Data,
      RequiredSignatures.Data,
      WebAPReplacedSelectionFieldValues.Data
{
   abstract class AddProjectConcern
         extends ConcernOf<AccessPoint>
         implements AccessPoint
   {

      @This
      SelectedForms.Data forms;

      public void changedProject(Project project)
      {
         next.changedProject(project);
         removeCaseType();
         for (Form form : forms.selectedForms().toList())
         {
            removeSelectedForm( form );
         }
      }
   }

   abstract class AddCaseTypeConcern
         extends ConcernOf<AccessPoint>
         implements AccessPoint
   {
      @This
      Labelable.Data labels;

      @This
      SelectedForms.Data forms;

      public void changedCaseType(CaseType caseType)
      {
         next.changedCaseType(caseType);
         List<Label> labelList = labels.labels().toList();
         for (Label label : labelList)
         {
            removeLabel( label );
         }

         for (Form form : forms.selectedForms().toList())
         {
            removeSelectedForm( form );
         }
      }
   }

   abstract class RemoveCaseTypeConcern
         extends ConcernOf<AccessPoint>
         implements AccessPoint
   {
      @This
      Labelable.Data labels;

      @This
      SelectedForms.Data forms;

      public void removeCaseType()
      {
         next.removeCaseType();
         List<Label> labelList = labels.labels().toList();
         for (Label label : labelList)
         {
            removeLabel( label );
         }

         for (Form form : forms.selectedForms().toList())
         {
            removeSelectedForm( form );
         }
      }
   }

}