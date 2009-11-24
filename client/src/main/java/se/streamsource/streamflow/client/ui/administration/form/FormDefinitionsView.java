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

package se.streamsource.streamflow.client.ui.administration.form;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FormDefinitionsView
        extends JPanel
{
    FormDefinitionsModel model;

    @Service
    DialogService dialogs;

    @Uses
    Iterable<NameDialog> nameDialogs;

    @Uses
    Iterable<ConfirmationDialog> confirmationDialog;

    public JList formList;

    public FormDefinitionsView(@Service ApplicationContext context, @Uses final FormDefinitionsModel model)
    {
        super(new BorderLayout());
        this.model = model;

        setActionMap(context.getActionMap(this));

        formList = new JList(model);

        formList.setCellRenderer(new ListItemListCellRenderer());
        add( formList, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(getActionMap().get("add")));
        toolbar.add(new JButton(getActionMap().get("remove")));
        add(toolbar, BorderLayout.SOUTH);
    }

    public JList getFormList()
    {
        return formList;
    }

    @Action
    public void add()
    {
        NameDialog dialog = nameDialogs.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog);
        String name = dialog.name();
        if (name != null)
        {
            model.createForm(name);
            model.refresh();
        }
    }

    @Action
    public void remove()
    {
        ConfirmationDialog dialog = confirmationDialog.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog, i18n.text(StreamFlowResources.confirmation));
        if(dialog.isConfirmed())
        {
            ListItemValue selected = (ListItemValue) formList.getSelectedValue();
            model.removeForm(selected.entity().get().identity());
            model.refresh();
        }
    }

}