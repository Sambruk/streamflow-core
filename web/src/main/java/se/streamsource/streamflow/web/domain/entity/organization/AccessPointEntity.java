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

package se.streamsource.streamflow.web.domain.entity.organization;

import org.qi4j.api.concern.*;
import org.qi4j.api.injection.scope.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.web.domain.entity.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;
import se.streamsource.streamflow.web.domain.structure.attachment.*;
import se.streamsource.streamflow.web.domain.structure.casetype.*;
import se.streamsource.streamflow.web.domain.structure.form.*;
import se.streamsource.streamflow.web.domain.structure.label.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.project.*;

import java.util.*;

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
      Removable.Data
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