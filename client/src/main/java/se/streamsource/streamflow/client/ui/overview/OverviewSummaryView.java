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

import java.awt.BorderLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;

import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.infrastructure.export.AbstractExporterFactory;
import se.streamsource.streamflow.client.infrastructure.export.ExcelExporter;
import se.streamsource.streamflow.client.infrastructure.export.ProjectSummaryExporterFactory;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

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
		addToolbarButton(toolBar, "Export");

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
	public void export() throws ResourceException
	{
		// TODO Excel or PDF choice - do pdf export
		// Export to excel
		// Ask the user where to save the exported file on disk
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		FileFilter filter = new FileNameExtensionFilter("Excel file", "xls",
				"ods", "sxc");
		fileChooser.addChoosableFileFilter(filter);
		int returnVal = fileChooser.showSaveDialog(OverviewSummaryView.this);
		if (returnVal != JFileChooser.APPROVE_OPTION)
		{
			return;
		}
		File file = fileChooser.getSelectedFile();

		// Do the actual excel exporting
		AbstractExporterFactory factory = new ProjectSummaryExporterFactory();
		ExcelExporter exporter = factory.createExcelExporter();
		exporter.export(model.getProjectOverviews(), file);

		// Show export confirmation to user and give option to open file.
		int response = JOptionPane
				.showConfirmDialog(
						OverviewSummaryView.this,
						"<html>The data was successfully exported to:<br/><br/>"
								+ file.getAbsolutePath()
								+ "<br/><br/>Do you want to open the exported file now?</html>",
						"Export completed!", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION)
		{
			try
			{
				Runtime.getRuntime().exec(
						new String[] { "open", file.getAbsolutePath() });
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
