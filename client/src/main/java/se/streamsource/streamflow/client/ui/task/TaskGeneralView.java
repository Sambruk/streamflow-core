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

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.COMBOBOX;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.DATEPICKER;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.DELETABLELABELSLIST;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.LABEL;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.TEXTAREA;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.TEXTFIELD;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutFocusTraversalPolicy;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;

import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.DeletableLabelsList;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * JAVADOC
 */
public class TaskGeneralView extends JScrollPane implements Observer
{
	@Service
	UncaughtExceptionHandler exception;

	private StateBinder taskBinder;

	TaskGeneralModel model;

	public ValueBuilder<TaskGeneralDTO> valueBuilder;
	public JTextField descriptionField;
	private JScrollPane notePane;
	public JXDatePicker dueOnField;
	private JToggleButton editButton;
	private JLabel issueLabel;
	public JPanel form;
	public DeletableLabelsList labels;
	public JXList selectedLabels;
	public JComboBox availableLabels;
	
	public TaskGeneralView(@Service ApplicationContext appContext)
    {
        setActionMap(appContext.getActionMap(this));
        FormLayout layout = new FormLayout(
                "200dlu",
                "");
        form = new JPanel();
        form.setFocusable(false);
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, form);
        builder.setDefaultDialogBorder();

        taskBinder = new StateBinder();
        taskBinder.setResourceMap(appContext.getResourceMap(getClass()));
        TaskGeneralDTO template = taskBinder.bindingTemplate(TaskGeneralDTO.class);

        notePane = (JScrollPane)TEXTAREA.newField();
        notePane.setSize(10,50);

        BindingFormBuilder bb = new BindingFormBuilder(builder, taskBinder);
        bb.appendLine(WorkspaceResources.id_label, issueLabel = (JLabel) LABEL.newField(), template.taskId());
        
        bb.appendLine(WorkspaceResources.description_label, descriptionField = (JTextField) TEXTFIELD.newField(), template.description())
                .appendLine(WorkspaceResources.note_label, notePane, template.note())
                .appendLine(WorkspaceResources.due_on_label, dueOnField = (JXDatePicker) DATEPICKER.newField(), template.dueOn())
                .appendLine(WorkspaceResources.labels_label, labels = (DeletableLabelsList) DELETABLELABELSLIST.newField(), template.labels());

        availableLabels = (JComboBox) COMBOBOX.newField();

        add(availableLabels);
        
        setViewportView(form);

        taskBinder.addObserver(this);

        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        setFocusCycleRoot(true);
        setFocusable(true);

        addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                Component defaultComp = getFocusTraversalPolicy().getDefaultComponent(form);
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
		
		labels.setListValue(general.labels().get());
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
			try
			{
				model.removeLabel(property.get().toString());
			} catch (ResourceException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			model.refresh();
		}
	}
}