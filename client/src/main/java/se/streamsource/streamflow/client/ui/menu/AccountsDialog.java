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

package se.streamsource.streamflow.client.ui.menu;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * JAVADOC
 */
public class AccountsDialog
        extends JPanel
{
    AccountsModel model;

    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    ObjectBuilderFactory obf;

    @Service
    IndividualRepository individualRepository;

    @Service
    DialogService dialogs;

    public JList accountList;

    @Uses
    Iterable<CreateAccountDialog> createAccountDialog;

    public AccountsDialog(@Service ApplicationContext context,
                          @Uses AccountsModel model)
    {
        super(new BorderLayout());
        this.model = model;

        setActionMap(context.getActionMap(this));

        accountList = new JList(model);
        accountList.setMinimumSize(new Dimension(200,300));
        accountList.setCellRenderer(new ListItemCellRenderer());

        add(new JScrollPane(accountList), BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(getActionMap().get("add")));
        toolbar.add(new JButton(getActionMap().get("remove")));
        add(toolbar, BorderLayout.SOUTH);

        accountList.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(getActionMap().get("remove")));
    }

    @Action
    public void execute()
    {
        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void add() throws ResourceException, UnitOfWorkCompletionException
    {
        CreateAccountDialog dialog = createAccountDialog.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog);

        if (dialog.settings() != null)
        {
            model.newAccount(dialog.settings());
        }
    }

    @Action
    public void remove() throws UnitOfWorkCompletionException
    {
        System.out.println("DeleteAccount invoked");
        model.removeAccount(accountList.getSelectedIndex());
    }

}