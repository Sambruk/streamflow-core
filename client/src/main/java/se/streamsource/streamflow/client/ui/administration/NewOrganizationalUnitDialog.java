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

package se.streamsource.streamflow.client.ui.administration;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.application.administration.command.NewOrganizationalUnitContext;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class NewOrganizationalUnitDialog
        extends JPanel
{
    @Structure
    UnitOfWorkFactory uowf;
    ValueBuilder<NewOrganizationalUnitContext> contextBuilder;
    public JTextField nameField;

    public NewOrganizationalUnitDialog(@Service ApplicationContext context, @Uses ValueBuilder<NewOrganizationalUnitContext> contextBuilder)
    {
        super(new BorderLayout());

        setActionMap(context.getActionMap(this));

        this.contextBuilder = contextBuilder;

        JPanel dialog = new JPanel(new BorderLayout());
        dialog.add(new JLabel("#name"), BorderLayout.WEST);
        nameField = new JTextField();
        dialog.add(nameField, BorderLayout.CENTER);
        add(dialog, BorderLayout.NORTH);
    }

    @Action
    public void execute()
    {
        try
        {
            /*contextBuilder.prototype().description().set(nameField.getText());

            NewOrganizationalUnitContext context = contextBuilder.newInstance();
            commands.newOrganizationalUnit(context);
            WindowUtils.findJDialog(this).dispose();

            uowf.currentUnitOfWork().discard();

            outlineController.refresh();*/

        } catch (Exception e)
        {
            // TODO
            e.printStackTrace();
        }
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}