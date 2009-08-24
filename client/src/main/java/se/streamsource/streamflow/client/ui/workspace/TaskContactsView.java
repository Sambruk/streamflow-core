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

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.UncaughtExceptionHandler;
import se.streamsource.streamflow.client.resource.users.workspace.user.task.TaskContactsClientResource;
import se.streamsource.streamflow.resource.task.TaskContactDTO;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * JAVADOC
 */
public class TaskContactsView
        extends JPanel
    implements Observer
{
    @Service
    UncaughtExceptionHandler exception;

    TaskContactsModel model;

    public JList contacts;
    private TaskContactModel contactModel;
    private TaskContactView contactView;
    private JButton removeButton;

    public TaskContactsView(@Service ApplicationContext context,
                            @Structure ObjectBuilderFactory obf)
    {
        super(new BorderLayout());

        ActionMap am = context.getActionMap(this);
        setActionMap(am);
        contactModel = new TaskContactModel();
        contactView  = obf.newObjectBuilder(TaskContactView.class).use(contactModel).newInstance();
        contactModel.addObserver(this);
        contactModel.addObserver(contactView);

        contacts = new JList();
        contacts.setMinimumSize(new Dimension(150,0));
        contacts.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1)
            {
                TaskContactDTO contact = (TaskContactDTO) o;
                return super.getListCellRendererComponent(jList, contact.name().get(), i, b, b1);
            }
        });
        add(contacts, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        toolbar.add(removeButton = new JButton(am.get("remove")));
        add(toolbar, BorderLayout.SOUTH);
        removeButton.setEnabled(false);
    }

    @org.jdesktop.application.Action
    public void add()
    {
        model.addContact();
    }

    @org.jdesktop.application.Action
    public void remove()
    {
        model.removeElement(getContactsList().getSelectedIndex());
        // set the selection in the smaller list
        if (model.contacts.size() == 0)
        {
            getContactsList().clearSelection();
        } else
        {
            if (model.contacts.size() <= getContactsList().getSelectedIndex())
                getContactsList().setSelectedIndex(model.contacts.size()-1);
            TaskContactDTO contact = (TaskContactDTO) model.getElementAt(getContactsList().getSelectedIndex());
            contactModel.setTaskContactDTO(contact);
        }

    }

    public JList getContactsList()
    {
        return contacts;
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

    public void setModel(TaskContactsModel model)
    {
        this.model = model;
        if (model != null)
        {
            contacts.setModel(model);
        }


    }

    public void update(Observable observable, Object o)
    {
        try
        {
            model.updateElement(contactModel.getContact(),getContactsList().getSelectedIndex());
            model.refresh();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }
    }

    public TaskContactsClientResource getTaskContactsClientResource()
    {
        return model.getTaskContactsClientResource();
    }

    public TaskContactModel getContactModel()
    {
        return contactModel;
    }

    public TaskContactView getContactView()
    {
        return contactView;
    }

    public void enableRemoveAccount(boolean enable)
    {

        removeButton.setEnabled(enable);
    }
}