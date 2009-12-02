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

package se.streamsource.streamflow.client.ui.administration.projects.forms;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * JAVADOC
 */
public class FieldsView
    extends JPanel
{
    private JList fieldList;

    @Service
    DialogService dialogs;

    @Uses
    Iterable<FieldCreationDialog> fieldCreationDialog;

    @Uses
    Iterable<ConfirmationDialog> confirmationDialog;

    private FieldsModel model;
    private JButton upButton;
    private JButton downButton;

    public FieldsView(@Service ApplicationContext context,
                      @Uses FieldsModel model)
    {
        super(new BorderLayout());
        this.model = model;
        JScrollPane scrollPanel = new JScrollPane();
        ActionMap am = context.getActionMap(this);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("add")));
        toolbar.add(new JButton(am.get("remove")));
        upButton = new JButton(am.get("up"));
        toolbar.add(upButton);
        downButton = new JButton(am.get("down"));
        toolbar.add(downButton);
        upButton.setEnabled(false);
        downButton.setEnabled(false);

        model.refresh();
        fieldList = new JList(model);
        fieldList.setCellRenderer(new ListItemListCellRenderer());

        scrollPanel.setViewportView(fieldList);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(new JSeparator(), BorderLayout.NORTH);
        titlePanel.add(new JLabel(i18n.text(AdministrationResources.fields_label)), BorderLayout.CENTER);

        add(titlePanel, BorderLayout.NORTH);
        add(scrollPanel, BorderLayout.CENTER);
        add(toolbar, BorderLayout.SOUTH);

        fieldList.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("remove")));
        fieldList.addListSelectionListener(new ListSelectionListener()
        {

            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    int idx = fieldList.getSelectedIndex();

                    upButton.setEnabled(idx != 0);
                    downButton.setEnabled(idx != fieldList.getModel().getSize()-1);
                    if (idx == -1)
                    {
                        upButton.setEnabled(false);
                        downButton.setEnabled(false);
                    }
                }
            }
        });
    }

    @org.jdesktop.application.Action
    public void add()
    {
        FieldCreationDialog dialog = fieldCreationDialog.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog, "Add new field to form");

        if (dialog.name()!=null && !"".equals(dialog.name()))
        {
            model.addField(dialog.name(), dialog.getFieldType());
        }
    }


    @org.jdesktop.application.Action
    public void remove()
    {
        int index = fieldList.getSelectedIndex();
        if (index != -1)
        {
            ConfirmationDialog dialog = confirmationDialog.iterator().next();
            dialogs.showOkCancelHelpDialog(this, dialog, i18n.text(StreamFlowResources.confirmation));
            if(dialog.isConfirmed())
            {
                model.removeField(index);
                fieldList.clearSelection();
            }
        }
    }

    @org.jdesktop.application.Action
    public void up()
    {
        int index = fieldList.getSelectedIndex();
        if (index>0 && index<fieldList.getModel().getSize())
        {
            model.moveField(index, index -1);
            fieldList.setSelectedIndex(index-1);
        }
    }

    @org.jdesktop.application.Action
    public void down()
    {
        int index = fieldList.getSelectedIndex();
        if (index>=0 && index<fieldList.getModel().getSize()-1)
        {
            model.moveField(index, index +1);
            fieldList.setSelectedIndex(index+1);
        }
    }

    public JList getFieldList()
    {
        return fieldList;
    }
}