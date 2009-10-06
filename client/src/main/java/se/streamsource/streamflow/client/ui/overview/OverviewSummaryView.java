/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.overview;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.FileNameExtensionFilter;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class OverviewSummaryView extends JPanel
{
	@Service
	protected DialogService dialogs;

	@Service
	protected StreamFlowApplication application;

	protected JXTable overviewSummaryTable;
	protected OverviewSummaryModel model;

	public void init(@Service ApplicationContext context,
			@Uses final OverviewSummaryModel model,
			@Structure final ObjectBuilderFactory obf,
			@Structure ValueBuilderFactory vbf)
	{
		this.model = model;
		model.refresh();
		setLayout(new BorderLayout());

		ActionMap am = context.getActionMap(OverviewSummaryView.class, this);
		setActionMap(am);

		// Table
		overviewSummaryTable = new JXTable(model);
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
	}

	protected Action addToolbarButton(JPanel toolbar, String name)
	{
		ActionMap am = getActionMap();
		Action action = am.get(name);
		action.putValue(Action.SMALL_ICON, i18n.icon((ImageIcon) action
				.getValue(Action.SMALL_ICON), 16));
		toolbar.add(new JButton(action));
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
                text(StreamFlowResources.excel_file), "xls"));
        int returnVal = fileChooser.showSaveDialog(OverviewSummaryView.this);
        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

        // Generate Excel file on the server.
		InputStream inputStream = model.getResource().generateExcelProjectSummary();

		File file = fileChooser.getSelectedFile();
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int count;
            while (((count = inputStream.read( buffer ))) != -1)
            {
                out.write( buffer, 0, count );
            }
        } finally
        {
            inputStream.close();
            if (out != null)
                out.close();
        }

		// Show export confirmation to user and give option to open file.
//		JXLabel confirmLabel = new JXLabel(i18n
//				.text(StreamFlowResources.export_data_file_with_open_option), (Icon)i18n
//				.icon(Icons.metadata), JXLabel.LEFT);
//		dialogs.showOkCancelHelpDialog(WindowUtils.findWindow(this),
//				confirmLabel, text(StreamFlowResources.export_completed));

		int response = JOptionPane.showConfirmDialog(OverviewSummaryView.this,
				text(StreamFlowResources.export_data_file_with_open_option),
				text(StreamFlowResources.export_completed),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		// <html>The data was successfully exported to:<br/><br/>"
		// + file.getAbsolutePath()
		// + "<br/><br/>Do you want to open the exported file now?</html>"
		if (response == JOptionPane.YES_OPTION)
		{
			Runtime.getRuntime().exec(
					new String[] { "open", file.getAbsolutePath() });
		}
	}
}
