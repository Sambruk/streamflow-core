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

package se.streamsource.streamflow.client.ui.caze;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.client.infrastructure.ui.UIAssemblers;
import se.streamsource.streamflow.client.ui.caze.conversations.AllParticipantsModel;
import se.streamsource.streamflow.client.ui.caze.conversations.AllParticipantsView;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationModel;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationParticipantsModel;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationParticipantsView;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationView;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationsModel;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationsView;
import se.streamsource.streamflow.client.ui.caze.conversations.MessagesModel;
import se.streamsource.streamflow.client.ui.caze.conversations.MessagesView;

/**
 * JAVADOC
 */
public class CaseAssembler
      implements Assembler
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      UIAssemblers.addViews( module, CasesView.class, CasesDetailView2.class, ContactsAdminView.class,
            FormsAdminView.class, SubmittedFormsAdminView.class );

      UIAssemblers.addDialogs( module, AddCommentDialog.class, CaseLabelsDialog.class );

      UIAssemblers.addMV( module, CasesTableModel.class, CaseTableView.class );

      UIAssemblers.addMV( module, CaseInfoModel.class, CaseInfoView.class );

      UIAssemblers.addModels( module, CasesModel.class, CaseFormsModel.class );

      UIAssemblers.addMV( module,
            CaseModel.class,
            CaseDetailView.class );

      UIAssemblers.addMV( module,
            ContactsModel.class,
            ContactsView.class );

      UIAssemblers.addMV( module,
            ContactModel.class,
            ContactView.class );

      UIAssemblers.addMV( module,
            CaseGeneralModel.class,
            CaseGeneralView.class );

      UIAssemblers.addMV( module,
            CaseLabelsModel.class,
            CaseLabelsView.class );

      UIAssemblers.addMV( module,
            PossibleCaseTypesModel.class,
            CaseTypesDialog.class );

      UIAssemblers.addMV( module,
            CaseEffectiveFieldsValueModel.class,
            CaseEffectiveFieldsValueView.class );

      UIAssemblers.addMV( module,
            CaseSubmittedFormsModel.class,
            CaseSubmittedFormsView.class );

      UIAssemblers.addMV( module,
            CaseSubmittedFormModel.class,
            CaseSubmittedFormView.class );

      UIAssemblers.addMV( module,
            FormSubmissionModel.class,
            FormSubmissionWizardPage.class );

      UIAssemblers.addMV( module,
              PossibleFormsModel.class,
              PossibleFormsView.class );

      UIAssemblers.addMV( module, 
    		  CaseActionsModel.class,
    		  CaseActionsView.class );

      // conversations
      UIAssemblers.addMV( module,
            AllParticipantsModel.class,
            AllParticipantsView.class );

      UIAssemblers.addMV( module,
            MessagesModel.class,
            MessagesView.class );

      UIAssemblers.addMV( module,
            ConversationModel.class,
            ConversationView.class );

      UIAssemblers.addMV( module,
            ConversationsModel.class,
            ConversationsView.class );

      UIAssemblers.addMV( module,
            ConversationParticipantsModel.class,
            ConversationParticipantsView.class );

   }
}
