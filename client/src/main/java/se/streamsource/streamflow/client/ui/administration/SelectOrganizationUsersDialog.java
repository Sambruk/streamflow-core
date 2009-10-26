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

package se.streamsource.streamflow.client.ui.administration;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;

import javax.swing.*;
import java.awt.*;

public class SelectOrganizationUsersDialog
    extends JPanel
{

    private ValueBuilderFactory vbf;

    private ListValue selectedUsers;
    private SelectOrganizationUsersDialogModel model;

    private JList list;

    public SelectOrganizationUsersDialog(@Service ApplicationContext context,
                             @Uses SelectOrganizationUsersDialogModel model,
                             @Structure ValueBuilderFactory vbf)
    {
        super(new BorderLayout());
        this.model = model;
        this.vbf = vbf;

        setActionMap(context.getActionMap(this));

        list = new JList(model);
        list.setCellRenderer(new ListItemCellRenderer());

        JScrollPane scrollPane = new JScrollPane(list);
        add(scrollPane, BorderLayout.CENTER);
        setPreferredSize(new Dimension(200,300));
    }


    @org.jdesktop.application.Action
    public void execute()
    {
        ListValueBuilder listBuilder = new ListValueBuilder(vbf);

        for(Object value : list.getSelectedValues())
        {
            ListItemValue user = (ListItemValue) value;
            listBuilder.addListItem(user.description().get(), user.entity().get());
        }
        selectedUsers = listBuilder.newList();
        
        WindowUtils.findWindow(this).dispose();
    }

    @org.jdesktop.application.Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }

    public ListValue getSelectedUsers()
    {
        return selectedUsers;
    }
}
