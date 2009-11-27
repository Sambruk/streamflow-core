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

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FieldsView
    extends JScrollPane
{
    private JList fieldList;

    @Service
    DialogService dialogs;

    @Uses
    Iterable<FieldCreationDialog> fieldCreationDialog;
    private FieldsModel model;

    public FieldsView(@Service ApplicationContext context,
                      @Uses FieldsModel model)
    {
        this.model = model;
        JPanel panel = new JPanel(new BorderLayout());
        ActionMap am = context.getActionMap(this);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        //toolbar.add(new JButton(am.get("remove")));
        //toolbar.add(new JButton(am.get("up")));
        //toolbar.add(new JButton(am.get("down")));

        model.refresh();
        fieldList = new JList(model);
        fieldList.setCellRenderer(new ListItemListCellRenderer());

        panel.add(fieldList, BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.SOUTH);

        setViewportView(panel);
        //fieldList.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("remove")));
        //fieldList.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("up")));
        //fieldList.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("down")));
    }

    @org.jdesktop.application.Action
    public void add()
    {
        FieldCreationDialog dialog = fieldCreationDialog.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog, "Add new field to form");

        if (dialog.getName()!=null && !"".equals(dialog.getName()))
        {
            model.addField(dialog.getName(), dialog.getFieldType());
        }
    }


    @org.jdesktop.application.Action
    public void remove()
    {
        int index = fieldList.getSelectedIndex();
        if (index != -1)
        {
            model.removeField(index);            
        }
    }

    @org.jdesktop.application.Action
    public void up()
    {

    }

    @org.jdesktop.application.Action
    public void down()
    {

    }

    public JList getFieldList()
    {
        return fieldList;
    }
}