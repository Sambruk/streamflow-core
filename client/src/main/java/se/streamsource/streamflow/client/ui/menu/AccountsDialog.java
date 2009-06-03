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
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class AccountsDialog
        extends JPanel
{
    AccountsModel model;

    @Structure
    ValueBuilderFactory vbf;

    @Structure
    ObjectBuilderFactory obf;

    @Service
    DialogService dialogs;

    @Structure
    UnitOfWorkFactory uowf;
    public JList accountList;

    public AccountsDialog(@Service ActionMap am,
                          @Service AccountsModel model)
    {
        super(new BorderLayout());
        this.model = model;

        setActionMap(am);

        accountList = new JList(model);
        accountList.setCellRenderer(new ListItemCellRenderer());

        add(accountList, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("createAccount")));
        toolbar.add(new JButton(am.get("deleteAccount")));
        add(toolbar, BorderLayout.SOUTH);

        /*accountList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                ListItemValue value = (ListItemValue) accountList.getSelectedValue();
            }
        });*/
    }

    @Action
    public void execute()
    {
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void deleteAccount()
    {
        System.out.println("DeleteAccount invoked");
        //accountList.getSelectedValue(); ...
        //model.removeAccount();
    }

}