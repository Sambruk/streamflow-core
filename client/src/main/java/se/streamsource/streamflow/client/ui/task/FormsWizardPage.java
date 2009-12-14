/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

import org.jdesktop.application.ApplicationContext;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelNavResult;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * JAVADOC
 */
public class FormsWizardPage
      extends WizardPage
{
   public final static String SELECTED_FORM = "SELECTED_FORM";

   public JList getFormList()
   {
      return formList;
   }

   private JList formList;
   FormsListModel model;

   public FormsWizardPage(@Service ApplicationContext context,
                          @Uses FormsListModel model)
   {
      super(FormSubmitWizardController.FORMS_SELECTION_STEP
            ,i18n.text(WorkspaceResources.form_select_description));
      ActionMap am = context.getActionMap(this);
      setActionMap(am);
      this.model = model;
      model.refresh();
      setLayout(new BorderLayout());
      formList = new JList(model);
      formList.setName(SELECTED_FORM);
      formList.setCellRenderer(new ListItemListCellRenderer());
      JScrollPane scroll = new JScrollPane();
      scroll.setViewportView(formList);
      add(scroll, BorderLayout.CENTER);
      addAncestorListener(new RefreshWhenVisible(model, this));
   }

   public FormsListModel getModel()
   {
      return model;
   }

   @Override
   public WizardPanelNavResult allowNext(String s, Map map, Wizard wizard)
   {
      if (formList.getSelectedValue() == null)
      {
         setProblem(i18n.text(WorkspaceResources.select_form));
         return WizardPanelNavResult.REMAIN_ON_PAGE;
      }
      return WizardPanelNavResult.PROCEED;
   }
}