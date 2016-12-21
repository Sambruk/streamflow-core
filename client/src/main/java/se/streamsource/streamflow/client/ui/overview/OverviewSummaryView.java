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
package se.streamsource.streamflow.client.ui.overview;

import static se.streamsource.streamflow.client.ui.overview.OverviewResources.assigned_column_header;
import static se.streamsource.streamflow.client.ui.overview.OverviewResources.inbox_column_header;
import static se.streamsource.streamflow.client.ui.overview.OverviewResources.project_column_header;
import static se.streamsource.streamflow.client.ui.overview.OverviewResources.total_column_header;
import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.BorderLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import se.streamsource.streamflow.client.util.StreamflowButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.restlet.representation.Representation;

import se.streamsource.dci.value.table.RowValue;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.util.FileNameExtensionFilter;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;

public class OverviewSummaryView extends JPanel
{
   @Service
   protected DialogService dialogs;

   @Service
   protected StreamflowApplication application;

   protected JXTable overviewSummaryTable;

   protected OverviewSummaryModel model;

   public void init(@Service ApplicationContext context,
                    @Uses final OverviewSummaryModel model)
   {
      this.model = model;
      setLayout(new BorderLayout());

      ActionMap am = context.getActionMap(OverviewSummaryView.class, this);
      setActionMap(am);

      // Table
      overviewSummaryTable = new JXTable(new EventJXTableModel<RowValue>(model.getProjectOverviews(), new TableFormat<RowValue>()
      {
         String[] columnNames = new String[]{text(project_column_header), text(inbox_column_header),
               text(assigned_column_header), text(total_column_header)};


         public int getColumnCount()
         {
            return columnNames.length;
         }

         public String getColumnName(int i)
         {
            return columnNames[i];
         }

         public Object getColumnValue(RowValue o, int i)
         {
            switch (i)
            {
               case 0:
                  return o.c().get().get(0).f().get();
               case 1:
                  return o.c().get().get(1).f().get();
               case 2:
                  return o.c().get().get(2).f().get();
               case 3:
                  return o.c().get().get(1).f().get() + o.c().get().get(2).f().get();
            }

            return null;
         }
      }));
      overviewSummaryTable.getActionMap().getParent().setParent(am);
      overviewSummaryTable.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys(
                        KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
      overviewSummaryTable.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                  .getDefaultFocusTraversalKeys(
                        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));

      JScrollPane overviewSummaryScrollPane = new JScrollPane(
            overviewSummaryTable);

      overviewSummaryTable.setAutoCreateColumnsFromModel(false);

      JPanel toolBar = new JPanel();
      addToolbarButton(toolBar, "export");

      add(overviewSummaryScrollPane, BorderLayout.CENTER);
      add(toolBar, BorderLayout.SOUTH);

      addFocusListener(new FocusAdapter()
      {
         public void focusGained(FocusEvent e)
         {
            overviewSummaryTable.requestFocusInWindow();
         }
      });

      new RefreshWhenShowing(this, model);
   }

   protected Action addToolbarButton(JPanel toolbar, String name)
   {
      ActionMap am = getActionMap();
      Action action = am.get(name);
      action.putValue(Action.SMALL_ICON, i18n.icon((ImageIcon) action
            .getValue(Action.SMALL_ICON), 16));
      toolbar.add(new StreamflowButton(action));
      return action;
   }

   @org.jdesktop.application.Action
   public void export()
         throws Exception
   {
      // TODO Excel or PDF choice - do pdf export
      // Export to excel
      // Ask the user where to save the exported file on disk
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser.setMultiSelectionEnabled(false);
      fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
            text(StreamflowResources.excel_file), true, "xls"));
      int returnVal = fileChooser.showSaveDialog(OverviewSummaryView.this);
      if (returnVal != JFileChooser.APPROVE_OPTION)
      {
         return;
      }

      // Generate Excel file on the server.
      Representation representation = model.generateExcelProjectSummary();

      File file = fileChooser.getSelectedFile();

      Inputs.byteBuffer(representation.getStream(), 8192).transferTo(Outputs.<Object>byteBuffer(file));

      int response = JOptionPane.showConfirmDialog(OverviewSummaryView.this,
            text(StreamflowResources.export_data_file_with_open_option),
            text(StreamflowResources.export_completed),
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      // <html>The data was successfully exported to:<br/><br/>"
      // + file.getAbsolutePath()
      // + "<br/><br/>Do you want to open the exported file now?</html>"
      if (response == JOptionPane.YES_OPTION)
      {
         Runtime.getRuntime().exec(
               new String[]{"open", file.getAbsolutePath()});
      }
   }
}
