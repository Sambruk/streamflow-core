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

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.DATEPICKER;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.LABEL;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.TEXTAREA;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.TEXTFIELD;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutFocusTraversalPolicy;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXDatePicker;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * JAVADOC
 */
public class TaskGeneralView extends JScrollPane implements Observer
{
	@Service
	UncaughtExceptionHandler exception;

    LabelSelectionView labelSelection;

	private StateBinder taskBinder;

	TaskGeneralModel model;

	public ValueBuilder<TaskGeneralDTO> valueBuilder;
	public JTextField descriptionField;
	private JScrollPane notePane;
	public JXDatePicker dueOnField;
	private JLabel issueLabel;
	public JPanel leftForm;
	public JPanel rightForm;
	public LabelsView labels;
	
	public TaskGeneralView(@Service ApplicationContext appContext, @Uses LabelsView labels)
    {
        this.labels = labels;
        this.labelSelection = labels.labelSelection();
        setActionMap(appContext.getActionMap(this));
        
        // Layout and form for the left panel
        FormLayout leftLayout = new FormLayout(
                "165dlu",
        		"");

        leftForm = new JPanel();
        leftForm.setFocusable(false);
        DefaultFormBuilder builder = new DefaultFormBuilder(leftLayout, leftForm);
        builder.setDefaultDialogBorder();

        taskBinder = new StateBinder();
        taskBinder.setResourceMap(appContext.getResourceMap(getClass()));
        TaskGeneralDTO template = taskBinder.bindingTemplate(TaskGeneralDTO.class);

        notePane = (JScrollPane)TEXTAREA.newField();
        notePane.setSize(10,50);

        BindingFormBuilder bb = new BindingFormBuilder(builder, taskBinder);
        bb.appendLine(WorkspaceResources.id_label, issueLabel = (JLabel) LABEL.newField(), template.taskId());
        
        bb.appendLine(WorkspaceResources.description_label, descriptionField = (JTextField) TEXTFIELD.newField(), template.description())
                .appendLine(WorkspaceResources.due_on_label, dueOnField = (JXDatePicker) DATEPICKER.newField(), template.dueOn())
                .appendLine(WorkspaceResources.note_label, notePane, template.note());

        // Layout and form for the right panel
        FormLayout rightLayout = new FormLayout(
                "165dlu:grow",
        		"pref, pref, pref");

        rightForm = new JPanel(rightLayout);
        rightForm.setFocusable(false);
        DefaultFormBuilder rightBuilder = new DefaultFormBuilder(rightLayout, rightForm);
        rightBuilder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();
        rightBuilder.add(new JLabel(i18n.text(WorkspaceResources.labels_label)), cc.xy(1, 1));
        rightBuilder.nextLine();
        rightBuilder.add(labels, cc.xy(1, 2));
        
        JPanel formsContainer = new JPanel(new BorderLayout());
        formsContainer.add(leftForm, BorderLayout.WEST);
        formsContainer.add(rightForm, BorderLayout.CENTER);
        setViewportView(formsContainer);

        taskBinder.addObserver(this);

        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        setFocusCycleRoot(true);
        setFocusable(true);

        addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                Component defaultComp = getFocusTraversalPolicy().getDefaultComponent(leftForm);
                if (defaultComp != null)
                {
                    defaultComp.requestFocusInWindow();
                }
            }

            public void focusLost(FocusEvent e)
            {
            }
        });
    }

	public void setModel(TaskGeneralModel taskGeneralModel)
	{
		model = taskGeneralModel;

		TaskGeneralDTO general = model.getGeneral();
		valueBuilder = general.buildWith();
		taskBinder.updateWith(general);

		// Check if issue id should be visible
		boolean issueVisible = model.getGeneral().taskId().get() != null;
		issueLabel.setVisible(issueVisible);
		((JLabel) issueLabel.getClientProperty("labeledBy"))
				.setVisible(issueVisible);

		labels.setLabelsModel(model.labelsModel());
        labelSelection.setLabelSelectionModel(model.selectionModel());
	}

	public void update(Observable o, Object arg)
	{
		Property property = (Property) arg;
		if (property.qualifiedName().name().equals("description"))
		{
			model.describe((String) property.get());
		} else if (property.qualifiedName().name().equals("note"))
		{
			model.changeNote((String) property.get());
		} else if (property.qualifiedName().name().equals("dueOn"))
		{
			model.changeDueOn((Date) property.get());
		} else if (property.qualifiedName().name().equals("labels")) 
		{
			model.removeLabel(property.get().toString());
		}
	}
}