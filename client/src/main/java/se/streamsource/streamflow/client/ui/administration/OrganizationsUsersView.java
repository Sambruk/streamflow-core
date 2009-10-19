/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.administration;

import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.FileNameExtensionFilter;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.ui.CreateUserDialog;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class OrganizationsUsersView
        extends JScrollPane
{
    private OrganizationsUsersModel model;

    @Uses
    Iterable<CreateUserDialog> userDialogs;


    @Service
    DialogService dialogs;

    public OrganizationsUsersView(@Service ApplicationContext context, @Uses OrganizationsUsersModel model)
    {
        ApplicationActionMap am = context.getActionMap(this);
        setActionMap(am);

        this.model = model;
        JXTable usersTable = new JXTable(model);
        usersTable.getColumn(0).setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
        usersTable.getColumn(0).setMaxWidth(30);
        usersTable.getColumn(0).setResizable(false);

        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.add(usersTable, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("createUser")));
        toolbar.add(new JButton(am.get("importUserList")));
        usersPanel.add(toolbar, BorderLayout.NORTH);

        setViewportView(usersPanel);
    }


    @org.jdesktop.application.Action
    public void createUser()
    {
        CreateUserDialog dialog = userDialogs.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog);

        if (dialog.username() != null && dialog.password() != null)
        {
            model.createUser(dialog.username(), dialog.password());
        }
    }

    @org.jdesktop.application.Action
    public void importUserList()
    {

        // Ask the user for a file to import user/pwd pairs from
        // Can be either Excels or CVS format
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                text(StreamFlowResources.all_files), "xls", "csv", "txt"));
        fileChooser.setDialogTitle(text(StreamFlowResources.import_users));
        int returnVal = fileChooser.showOpenDialog((OrganizationsUsersView.this));
        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

        model.importUsers(fileChooser.getSelectedFile().getAbsoluteFile());
           
    }

}