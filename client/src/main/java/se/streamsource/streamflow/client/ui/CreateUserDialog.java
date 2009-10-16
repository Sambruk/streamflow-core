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

package se.streamsource.streamflow.client.ui;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.*;
import java.awt.*;

/**
 * Select a name for something.
 */
public class CreateUserDialog
        extends JPanel
{
    public JTextField nameField;
    public JPasswordField passwordField;

    String username;
    String password;

    public CreateUserDialog(@Service ApplicationContext context)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));

        JPanel userDialog = new JPanel(new BorderLayout());
        userDialog.add(new JLabel(i18n.text(AdministrationResources.username_label)), BorderLayout.WEST);
        nameField = new JTextField();
        userDialog.add(nameField, BorderLayout.CENTER);

        JPanel passwordDialog = new JPanel(new BorderLayout());
        passwordDialog.add(new JLabel(i18n.text(AdministrationResources.password_label)), BorderLayout.WEST);
        passwordField = new JPasswordField();
        passwordDialog.add(passwordField, BorderLayout.CENTER);

        add(userDialog, BorderLayout.NORTH);
        add(passwordDialog, BorderLayout.CENTER);
    }

    public String username()
    {
        return username;
    }

    public String password()
    {
        return password;
    }

    @Action
    public void execute()
    {
        username = nameField.getText();
        password = new String(passwordField.getPassword());

        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}