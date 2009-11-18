/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.administration.organization;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.SelectOrganizationUsersDialog;
import se.streamsource.streamflow.client.ui.administration.SelectOrganizationUsersDialogModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;

import javax.swing.*;
import java.awt.*;

public class OrganizationUsersView
    extends JPanel
{
    @Structure
    ObjectBuilderFactory obf;

    @Structure
    ValueBuilderFactory vbf;

    @Service
    DialogService dialogs;

    public JList participantList;

    private OrganizationUsersModel model;

    public OrganizationUsersView(@Service ApplicationContext context, @Uses OrganizationUsersModel model)
    {
        super(new BorderLayout());
        this.model = model;

        ActionMap am = context.getActionMap(this);
        setActionMap(am);

        participantList = new JList(model);

        participantList.setCellRenderer(new ListItemListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(participantList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        toolbar.add(new JButton(am.get("remove")));
        add(toolbar, BorderLayout.SOUTH);

        participantList.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("remove")));
    }

    @org.jdesktop.application.Action
    public void add() throws ResourceException
    {
        SelectOrganizationUsersDialogModel dialogModel = obf.newObjectBuilder(SelectOrganizationUsersDialogModel.class)
                .use(model.getResource()).newInstance();
        SelectOrganizationUsersDialog dialog = obf.newObjectBuilder(SelectOrganizationUsersDialog.class)
                .use(dialogModel).newInstance();
        dialogs.showOkCancelHelpDialog(
                WindowUtils.findWindow(this),
                dialog,
                text(AdministrationResources.join_organization));

        if(dialog.getSelectedUsers() != null)
        {
            model.getResource().join(dialog.getSelectedUsers());
            model.refresh();
        }
    }

    @org.jdesktop.application.Action
    public void remove() throws ResourceException
    {
        ListValueBuilder userList = new ListValueBuilder(vbf);
        for( int index : participantList.getSelectedIndices())
        {
            ListItemValue user = (ListItemValue) model.getElementAt(index);
            userList.addListItem(user.description().get(), user.entity().get());
        }

        model.getResource().leave(userList.newList());
        model.refresh();

        participantList.clearSelection();
    }

    public JList getParticipantList()
    {
        return participantList;
    }
}
