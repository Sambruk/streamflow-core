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
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;
import se.streamsource.streamflow.client.infrastructure.ui.FormEditor;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
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

    public FormEditor editor;
    public ValueBuilder<TaskGeneralDTO> valueBuilder;
    public JTextField descriptionField;
    private JToggleButton editButton;

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

        BindingFormBuilder bb = new BindingFormBuilder(builder, taskBinder);
        bb
        .appendLine(WorkspaceResources.id_label, LABEL, template.taskId())
        .appendLine(WorkspaceResources.description_label, descriptionField = (JTextField) TEXTFIELD.newField(), template.description())
        .appendLine(WorkspaceResources.labels_label, LABEL, template.labels())
        .appendLine(WorkspaceResources.note_label, TEXTAREA, template.note())
        .appendLine(editButton = new JToggleButton(getActionMap().get("edit")));

        editor = new FormEditor(taskBinder.boundComponents());

        setViewportView(form);
    }

    @Action
    public void edit() throws ResourceException
    {
        if (!editor.isEditing())
            editor.edit();
        else
        {
            editor.view();

            // Update settings
            model.updateGeneral(valueBuilder.newInstance());
        }
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
        }
    }

    public void update(Observable o, Object arg)
    {
        TaskGeneralDTO general = model.getGeneral();
        valueBuilder = general.buildWith();
        taskBinder.updateWith(valueBuilder.prototype());
    }
}