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

package se.streamsource.streamflow.client.ui.administration.groups;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class NewGroupDialog
        extends JPanel
{
    @Service
    GroupsModel groupsModel;

    @Structure
    UnitOfWorkFactory uowf;

    public JTextField nameField;

    public NewGroupDialog(@Service ApplicationContext context)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));

        JPanel dialog = new JPanel(new BorderLayout());
        dialog.add(new JLabel("#name"), BorderLayout.WEST);
        nameField = new JTextField();
        dialog.add(nameField, BorderLayout.CENTER);
        add(dialog, BorderLayout.NORTH);
    }

    @Action
    public void execute()
    {
        groupsModel.newGroup(nameField.getText());
        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}
