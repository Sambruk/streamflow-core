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

package se.streamsource.streamflow.client.ui.workspace;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.JTextArea;
import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class TaskGeneralView
        extends JScrollPane
        implements Observer
{
    @Service
    UncaughtExceptionHandler exception;

    private StateBinder taskBinder;

    TaskGeneralModel model;

    public ValueBuilder<TaskGeneralDTO> valueBuilder;
    public JTextField descriptionField;
    private JToggleButton editButton;
    private JLabel issueLabel;

    public TaskGeneralView(@Service ApplicationContext appContext)
    {
        setActionMap(appContext.getActionMap(this));
        FormLayout layout = new FormLayout(
                "200dlu",
                "");
        JPanel form = new JPanel();
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, form);
        builder.setDefaultDialogBorder();

        taskBinder = new StateBinder();
        taskBinder.setResourceMap(appContext.getResourceMap(getClass()));
        TaskGeneralDTO template = taskBinder.bindingTemplate(TaskGeneralDTO.class);

        JTextArea noteField = new JTextArea(10, 50);
        noteField.setLineWrap(true);
                
        BindingFormBuilder bb = new BindingFormBuilder(builder, taskBinder);
        bb.appendLine(WorkspaceResources.id_label, issueLabel = (JLabel) LABEL.newField(), template.taskId());

        bb.appendLine(WorkspaceResources.description_label, descriptionField = (JTextField) TEXTFIELD.newField(), template.description())
//        .appendLine(WorkspaceResources.labels_label, LABEL, template.labels())

        .appendLine(WorkspaceResources.note_label, new JScrollPane(noteField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), template.note());

        setViewportView(form);

        taskBinder.addObserver(this);

        descriptionField.setFocusAccelerator('B');

        form.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        form.setFocusCycleRoot(true);
        form.setFocusable(true);
    }

    public void setModel(TaskGeneralModel taskGeneralModel)
    {
        if (model != null)
        {
            model.deleteObserver(this);
        }

        model = taskGeneralModel;

        if (model != null)
        {
            model.addObserver(this);

            update(model, null);

            // Check if issue id should be visible
            boolean issueVisible = model.getGeneral().taskId().get() != null;
            issueLabel.setVisible(issueVisible);
            ((JLabel)issueLabel.getClientProperty("labeledBy")).setVisible(issueVisible);
        }
    }

    public void update(Observable o, Object arg)
    {
        if (o == taskBinder)
        {
            Property property = (Property) arg;
            if (property.qualifiedName().name().equals("description"))
            {
                try
                {
                    model.describe((String) property.get());
                } catch (ResourceException e)
                {
                    throw new OperationException(WorkspaceResources.could_not_change_description, e);
                }
            } else if (property.qualifiedName().name().equals("note"))
            {
                try
                {
                    model.changeNote((String) property.get());
                } catch (ResourceException e)
                {
                    throw new OperationException(WorkspaceResources.could_not_change_note, e);
                }
            }
        } else
        {
            TaskGeneralDTO general = model.getGeneral();
            valueBuilder = general.buildWith();
            taskBinder.updateWith(general);
        }
    }

    @Override
    public void setVisible(boolean aFlag)
    {
        super.setVisible(aFlag);

        if (aFlag)
            descriptionField.grabFocus();
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
    }
}