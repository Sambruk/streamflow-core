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

package se.streamsource.streamflow.client.ui.administration.projects.members;

import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsModel;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectModel;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

/**
 * JAVADOC
 */
public class AddUsersView
        extends JPanel
{
    private AddUsersModel addUsersModel;
    private JTextField nameField;

    public AddUsersView(@Service final ProjectModel projectModel,
                        @Uses AddUsersModel addUsersModel,
                        @Structure ValueBuilderFactory vbf)
    {
        super(new BorderLayout());
        this.addUsersModel = addUsersModel;

        addUsersModel.setUsers(vbf.newValueBuilder(ListValue.class).newInstance());
        nameField = new JTextField();
        nameField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent keyEvent)
            {
                try
                {
                    ListValue list = projectModel.findPartcipants(nameField.getText());
                    getModel().setUsers(list);
                } catch (ResourceException e)
                {
                    e.printStackTrace();
                }
            }
        });
        nameField.setColumns(10);
        JPanel searchLine = new JPanel(new BorderLayout());
        searchLine.add(new JLabel("#Search users"), BorderLayout.CENTER);
        searchLine.add(nameField, BorderLayout.LINE_END);
        add(searchLine, BorderLayout.NORTH);
        JXTable usersTable = new JXTable(addUsersModel);
        usersTable.getColumn(0).setMaxWidth(40);
        usersTable.getColumn(0).setResizable(false);
        JScrollPane usersScrollPane = new JScrollPane(usersTable);
        add(usersScrollPane);
    }


    public AddUsersModel getModel()
    {
        return addUsersModel;
    }
}