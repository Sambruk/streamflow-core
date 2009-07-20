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

package se.streamsource.streamflow.client.ui.shared;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;
import se.streamsource.streamflow.client.infrastructure.ui.FormEditor;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class TaskGeneralView
        extends JScrollPane
{
    @Service
    UncaughtExceptionHandler exception;

    private StateBinder sharedTaskBinder;

    TaskGeneralModel model;

    public FormEditor editor;
    public ValueBuilder<TaskGeneralDTO> valueBuilder;

    public TaskGeneralView(@Service ApplicationContext appContext,
                          @Uses final TaskGeneralModel model)
    {
        this.model = model;
        setActionMap(appContext.getActionMap(this));
        FormLayout layout = new FormLayout(
                "200dlu",
                "");
        JPanel form = new JPanel();
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, form);
        builder.setDefaultDialogBorder();

        sharedTaskBinder = new StateBinder();
        sharedTaskBinder.setResourceMap(appContext.getResourceMap(getClass()));
        TaskGeneralDTO template = sharedTaskBinder.bindingTemplate(TaskGeneralDTO.class);

        BindingFormBuilder bb = new BindingFormBuilder(builder, sharedTaskBinder);
        bb
        .appendLine(WorkspaceResources.id_label, LABEL, template.taskId())
        .appendLine(WorkspaceResources.description_label, TEXTFIELD, template.description())
        .appendLine(WorkspaceResources.note_label, TEXTAREA, template.note())
        .appendToggleButtonLine(getActionMap().get("edit"));

        editor = new FormEditor(sharedTaskBinder.boundComponents());

        model.addObserver(new Observer()
        {
            public void update(Observable o, Object arg)
            {
                TaskGeneralDTO general = model.getGeneral();
                valueBuilder = general.buildWith();
                sharedTaskBinder.updateWith(valueBuilder.prototype());
            }
        });

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

    @Override
    public void setVisible(boolean aFlag)
    {
        super.setVisible(aFlag);

        if (aFlag)
            try
            {
                model.refresh();
            } catch (Exception e)
            {
                exception.uncaughtException(e);
            }
    }
}