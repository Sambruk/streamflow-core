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
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;
import se.streamsource.streamflow.client.infrastructure.ui.FormEditor;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.resource.inbox.InboxTaskGeneralValue;

import javax.swing.JPanel;
import java.io.IOException;

/**
 * JAVADOC
 */
public class SharedInboxGeneralTaskDetailView
        extends JPanel
{
    private StateBinder sharedTaskBinder;

    @Service
    SharedInboxTaskDetailModel model;
    public FormEditor editor;
    public ValueBuilder<InboxTaskGeneralValue> valueBuilder;

    public SharedInboxGeneralTaskDetailView(@Service ApplicationContext appContext)
    {
        setActionMap(appContext.getActionMap(this));
        FormLayout layout = new FormLayout(
                "200dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
        builder.setDefaultDialogBorder();

        sharedTaskBinder = new StateBinder();
        sharedTaskBinder.setResourceMap(appContext.getResourceMap(getClass()));
        InboxTaskGeneralValue template = sharedTaskBinder.bindingTemplate(InboxTaskGeneralValue.class);

        BindingFormBuilder bb = new BindingFormBuilder(builder, sharedTaskBinder);
        bb
        .appendLine(SharedInboxResources.description_label, TEXTFIELD, template.description())
        .appendLine(SharedInboxResources.note_label, TEXTAREA, template.note())
        .appendToggleButtonLine(getActionMap().get("edit"));

        editor = new FormEditor(sharedTaskBinder.boundComponents());
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
            model.sharedTask().put(new StringRepresentation(valueBuilder.newInstance().toJSON(), MediaType.APPLICATION_JSON));
        }
    }

    @Override
    public void setVisible(boolean aFlag)
    {
        super.setVisible(aFlag);

        if (aFlag)
        {
            try
            {
                if (model.sharedTask() != null)
                {
                    InboxTaskGeneralValue general = model.sharedTask().general();
                    valueBuilder = general.buildWith();
                    sharedTaskBinder.updateWith(valueBuilder.prototype());
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            } catch (ResourceException e)
            {
                e.printStackTrace();
            }
        }
    }
}