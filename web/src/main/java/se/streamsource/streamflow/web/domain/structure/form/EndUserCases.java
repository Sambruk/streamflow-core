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

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPointSettings;
import se.streamsource.streamflow.web.domain.structure.user.AnonymousEndUser;

/**
 * JAVADOC
 */
@Mixins(EndUserCases.Mixin.class)
public interface EndUserCases
{
   CaseEntity createCaseWithForm( AnonymousEndUser endUser );

   CaseEntity createCase( AnonymousEndUser endUser );

   void submitFormAndSendCase( Case caze, FormSubmission formSubmission, Submitter submitter );

   void submitForm( Case caze, FormSubmission formSubmission, Submitter submitter );

   void sendToFunction( Case caze );

   abstract class Mixin
         implements EndUserCases
   {
      @This
      SelectedForms.Data selectedForms;

      @This
      AccessPointSettings.Data accesspoint;

      @This
      Labelable.Data labelable;

      public CaseEntity createCaseWithForm( AnonymousEndUser endUser )
      {
         CaseEntity caseEntity = createCase( endUser );
         caseEntity.createFormSubmission( selectedForms.selectedForms().get( 0 ));
         return caseEntity;
      }

      public void submitForm( Case caze, FormSubmission formSubmission, Submitter submitter )
      {
         caze.submitForm( formSubmission, submitter );
      }

      public void submitFormAndSendCase( Case caze, FormSubmission formSubmission, Submitter submitter )
      {
         submitForm( caze, formSubmission, submitter );
         sendToFunction( caze );

      }

      public CaseEntity createCase( AnonymousEndUser endUser )
      {
         CaseEntity caseEntity = endUser.createDraft();
         caseEntity.changeDescription( accesspoint.caseType().get().getDescription() );
         caseEntity.changeCaseType( accesspoint.caseType().get() );

         for (Label label : labelable.labels())
         {
            caseEntity.addLabel( label );
         }
         return caseEntity;
      }

      public void sendToFunction( Case caze )
      {
         CaseEntity caseEntity = (CaseEntity) caze;
         caseEntity.unassign();
         caseEntity.changeOwner( (ProjectEntity) accesspoint.project().get() );
         caseEntity.open();
      }
   }
}