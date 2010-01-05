/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.task;

import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardPanelProvider;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import java.util.Map;

/**
 * JAVADOC
 */
public class FormSubmitWizardController
      extends WizardBranchController
{
   public static final String FORMS_SELECTION_STEP = "FORMS_SELECTION_STEP";

   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   private final FormsListModel model;

   public FormSubmitWizardController(@Uses FormsWizardPage formsListWizard)
   {
      super(new FormSelectionWizardPanelProvider(formsListWizard,
            i18n.text(WorkspaceResources.form_submit_wizard), FORMS_SELECTION_STEP,
            i18n.text(WorkspaceResources.form_select_description)));
      model = formsListWizard.getModel();
   }

   @Override
   protected WizardPanelProvider getPanelProviderForStep(String name, Map map)
   {
      if (map.get(FormsWizardPage.SELECTED_FORM) != null)
      {
         ListItemValue value = (ListItemValue) map.get(FormsWizardPage.SELECTED_FORM);

         FormSubmitModel formSubmitModel = model.getFormSubmitModel( value.entity().get().identity() );

         String title = value.description().get();
         formSubmitModel.setUpWizardPages();
         String[] pageIds = formSubmitModel.getPageIds();
         String[] pageNames = formSubmitModel.getPageNames();

         return new FormSubmissionWizardPanelProvider(obf, vbf, model.getFormSubmitModel(value.entity().get().identity()),
               title, pageIds, pageNames);
      }
      return null;
   }
}