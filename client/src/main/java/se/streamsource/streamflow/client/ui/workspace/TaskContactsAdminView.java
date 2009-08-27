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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.resource.task.TaskContactDTO;
import se.streamsource.streamflow.client.resource.users.workspace.user.task.TaskContactsClientResource;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;


/**
 * JAVADOC
 */
public class TaskContactsAdminView
        extends JPanel
{
    @Structure
    ObjectBuilderFactory obf;

    private TaskContactsView taskContactsView;

    public TaskContactsAdminView(@Uses final TaskContactsView taskContactsView)
    {
        super(new BorderLayout());

        this.taskContactsView = taskContactsView;
        add(taskContactsView, BorderLayout.WEST);
        add(taskContactsView.getContactView(), BorderLayout.CENTER);

        final JList list = taskContactsView.getContactsList();
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    int idx = list.getSelectedIndex();
                    if (idx != -1)
                    {
                        TaskContactDTO contactValue = (TaskContactDTO) list.getModel().getElementAt(idx);
                        TaskContactsClientResource taskContactsClientResource = taskContactsView.getTaskContactsResource();
                        TaskContactModel taskContactModel = obf.newObjectBuilder(TaskContactModel.class).use(contactValue, taskContactsClientResource.taskContact(idx)).newInstance();
                        taskContactsView.getContactView().setModel(taskContactModel);
                    } else
                    {
                        taskContactsView.getContactView().setModel(null);
                    }
                }
            }
        });

    }

    @Override
    public void setVisible(boolean aFlag)
    {
        super.setVisible(aFlag);
        taskContactsView.setVisible(aFlag);
    }

    public void setModel(TaskContactsModel taskContactsModel)
    {
        taskContactsView.setModel(taskContactsModel);
    }
}