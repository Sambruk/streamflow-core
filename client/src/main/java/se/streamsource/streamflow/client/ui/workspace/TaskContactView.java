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
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.resource.task.TaskContactDTO;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class TaskContactView
        extends JScrollPane
    implements Observer
{
    @Service
    UncaughtExceptionHandler exception;

    private StateBinder contactBinder;

    @Uses
    TaskContactModel model;

    public ValueBuilder<TaskContactDTO> valueBuilder;

    public TaskContactView(@Service ApplicationContext appContext)
    {
        setActionMap(appContext.getActionMap(this));
        FormLayout layout = new FormLayout(
                "200dlu",
                "");
        JPanel form = new JPanel();
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, form);
        builder.setDefaultDialogBorder();

        

        contactBinder = new StateBinder();
        contactBinder.setResourceMap(appContext.getResourceMap(getClass()));
        TaskContactDTO template = contactBinder.bindingTemplate(TaskContactDTO.class);

        BindingFormBuilder bb = new BindingFormBuilder(builder, contactBinder);
        bb
        .appendLine(WorkspaceResources.name_label, TEXTFIELD, template.name())
        .appendLine(WorkspaceResources.company_label, TEXTFIELD, template.company())
        .appendLine(WorkspaceResources.note_label, TEXTAREA, template.note());

        setViewportView(form);
        contactBinder.addObserver(this);
    }

    public void update(Observable observable, Object o)
    {
        if (observable != contactBinder)
        {
            TaskContactDTO contact = model.getContact();
            valueBuilder = contact.buildWith();
            contactBinder.updateWith(valueBuilder.prototype());
        } else
        {
            TaskContactDTO contact = valueBuilder.newInstance();
            model.setTaskContactDTO(contact);
        }
    }
}
