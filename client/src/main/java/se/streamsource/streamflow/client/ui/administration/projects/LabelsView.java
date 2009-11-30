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

package se.streamsource.streamflow.client.ui.administration.projects;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.JListPopup;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.LabelsModel;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class LabelsView
        extends JPanel
{
    @Service
    DialogService dialogs;

    @Uses
    Iterable<NameDialog> nameDialogs;

    public JList labelList;

    private LabelsModel model;

    public LabelsView(@Service ApplicationContext context, @Uses LabelsModel model)
    {
        super(new BorderLayout());
        this.model = model;

        ActionMap am = context.getActionMap(this);
        setActionMap(am);

        JPopupMenu popup = new JPopupMenu();
        popup.add(am.get("rename"));
        labelList = new JListPopup(model, popup);

        labelList.setCellRenderer(new ListItemListCellRenderer());

        add(labelList, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        toolbar.add(new JButton(am.get("remove")));
        add(toolbar, BorderLayout.SOUTH);
        labelList.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("remove")));
    }

    @Action
    public void add()
    {
        NameDialog dialog = nameDialogs.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog, text(AdministrationResources.add_label_title));
        String name = dialog.name();
        if (name != null)
        {
            model.newLabel(name);
        }
    }

    @Action
    public void remove()
    {
//        model.removeLabel(labelList.getSelectedIndex());
    }

    @Action
    public void rename() throws ResourceException
    {
        NameDialog dialog = nameDialogs.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog);

        if (dialog.name() != null)
        {
            model.describe(labelList.getSelectedIndex(), dialog.name());
        }
    }

    public JList getLabelList()
    {
        return labelList;
    }
}