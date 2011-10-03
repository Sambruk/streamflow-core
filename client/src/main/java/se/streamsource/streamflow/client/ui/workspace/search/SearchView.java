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

package se.streamsource.streamflow.client.ui.workspace.search;


import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.table.PerspectiveView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class SearchView
        extends JPanel
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   private JTextField searchField;

   private final SearchResultTableModel searchResultTableModel;

   private JPanel search;

   public SearchView(@Service ApplicationContext context,
                     @Uses SearchResultTableModel searchResultTableModel)
   {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      this.searchResultTableModel = searchResultTableModel;

      ActionMap am;
      setActionMap(am = context.getActionMap(this));

      javax.swing.Action searchAction = am.get("search");
      JButton searchButton = new JButton(searchAction);
      searchButton.registerKeyboardAction(searchAction, (KeyStroke) searchAction
              .getValue(javax.swing.Action.ACCELERATOR_KEY),
              JComponent.WHEN_IN_FOCUSED_WINDOW);

      searchField = new JTextField(40);
      searchField.addActionListener(searchAction);

      search = new JPanel(new FlowLayout(FlowLayout.LEFT));
      search.add(searchField);
      search.add(searchButton);
      add(search);

      new RefreshWhenShowing(this, new Refreshable()
      {
         public void refresh()
         {
            getTextField().requestFocusInWindow();
         }
      });

   }

   public JTextField getTextField()
   {
      return (JTextField) searchField;
   }

   @Action
   public Task search()
   {
      // close all open perspective popups without triggering search twice
      if (!closedOpenPerspectivePopups(WindowUtils.findWindow(this)))
      {
         final String searchString = getTextField().getText();

         if (searchString.length() > 500)
         {
            dialogs.showMessageDialog(this, i18n.text(WorkspaceResources.too_long_query), "");
         } else
         {
            return new Task<Void, Void>(Application.getInstance())
            {
               @Override
               protected Void doInBackground() throws Exception
               {
                  searchResultTableModel.search(searchString);
                  return null;
               }

               @Override
               protected void succeeded(Void result)
               {
               }

               @Override
               protected void failed(Throwable cause)
               {
               }
            };
         }
      }

      return null;
   }

   private boolean closedOpenPerspectivePopups(Container container)
   {
      for (Component c : container.getComponents())
      {
         if (c instanceof Container)
         {
            if (c instanceof PerspectiveView)
            {
               PerspectiveView view = ((PerspectiveView) c);
               if (view.getCurrentPopup() != null)
               {
                  view.killPopup();
                  view.cleanToggleButtonSelection();
                  return true;
               }
            } else
            {
               closedOpenPerspectivePopups((Container) c);
            }
         }
      }
      return false;
   }
}