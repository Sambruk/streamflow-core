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
import org.jdesktop.application.*;
import org.jdesktop.swingx.util.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import org.qi4j.api.structure.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.client.ui.workspace.*;
import se.streamsource.streamflow.client.ui.workspace.table.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;

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

   public SearchView(@Service ApplicationContext context, @Uses final CommandQueryClient client,
                     @Uses SearchResultTableModel searchResultTableModel, @Structure ObjectBuilderFactory obf)
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
   public void search()
   {
      // close all open perspective popups without triggering search twice
      if (!closedOpenPerspectivePopups(WindowUtils.findWindow(this)))
      {
         String searchString = getTextField().getText();

         if (searchString.length() > 500)
         {
            dialogs.showMessageDialog(this, i18n.text(WorkspaceResources.too_long_query), "");
         } else
         {
            searchResultTableModel.search(searchString);
         }
      }
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