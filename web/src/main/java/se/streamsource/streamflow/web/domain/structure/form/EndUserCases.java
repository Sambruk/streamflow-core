/**
 *
 * Copyright 2009-2012 Jayway Products AB
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

import static se.streamsource.streamflow.util.Strings.empty;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.api.workspace.cases.contact.ContactBuilder;
import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.api.workspace.cases.general.SecondSigneeInfoValue;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPointSettings;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;
import se.streamsource.streamflow.web.domain.structure.user.EndUser;

/**
 * JAVADOC
 */
@Mixins(EndUserCases.Mixin.class)
public interface EndUserCases
{
   CaseEntity createCaseWithForm( EndUser endUser );

   CaseEntity createCase( Drafts endUser );

   void submitFormAndSendCase( Case caze, FormDraft formSubmission, Submitter submitter );

   void submitForm( Case caze, FormDraft formSubmission, Submitter submitter );

   void sendTo( Case caze );

   void discardCase( Case caze );

   abstract class Mixin
         implements EndUserCases
   {
      @This
      SelectedForms.Data selectedForms;

      @This
      AccessPointSettings.Data accesspoint;

      @This
      Labelable.Data labelable;

      @Structure
      Module module;

      public CaseEntity createCaseWithForm( EndUser endUser )
      {
         CaseEntity caseEntity = createCase( endUser );
         caseEntity.createFormDraft( selectedForms.selectedForms().get( 0 ) );
         return caseEntity;
      }

      public void submitForm( Case caze, FormDraft formSubmission, Submitter submitter )
      {
         // Transfer contact information from submitter
         // TODO Also add from typed form data
         Contactable contactable = (Contactable) caze.createdBy().get();
         if (!empty(contactable.getContact().contactId().get()))
            caze.addContact(contactable.getContact());

         // Add contact info for signatories
         for (FormSignatureDTO signature : formSubmission.getFormDraftValue().signatures().get())
         {
            ContactBuilder builder = new ContactBuilder(module.valueBuilderFactory());
            builder.name(signature.signerName().get()).contactId(signature.signerId().get());
            caze.addContact(builder.newInstance());
         }

         if( formSubmission.getFormDraftValue().secondsignee().get() != null
            &&!formSubmission.getFormDraftValue().secondsignee().get().singlesignature().get() )
         {
            SecondSigneeInfoValue secondSignee = formSubmission.getFormDraftValue().secondsignee().get();
            ContactBuilder builder = new ContactBuilder( module.valueBuilderFactory() );
            builder.name( secondSignee.name().get() );
            builder.email( secondSignee.email().get() );
            builder.phoneNumber( secondSignee.phonenumber().get() );
            builder.contactId( secondSignee.socialsecuritynumber().get() );
            caze.addContact( builder.newInstance() );
         }

         // Submit the form
         caze.submitForm( formSubmission, submitter );
      }

      public void submitFormAndSendCase( Case caze, FormDraft formSubmission, Submitter submitter )
      {
         submitForm( caze, formSubmission, submitter );
         sendTo( caze );
      }

      public CaseEntity createCase( Drafts endUser )
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

      public void sendTo( Case caze )
      {
         CaseEntity caseEntity = (CaseEntity) caze;
         if (caseEntity.isAssigned())
            caseEntity.unassign();
         caseEntity.changeOwner( accesspoint.project().get() );
         caseEntity.open();
      }

      public void discardCase( Case caze )
      {
         caze.deleteEntity();
      }
   }
}