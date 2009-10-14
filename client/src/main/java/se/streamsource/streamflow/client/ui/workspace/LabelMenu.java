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

package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.task.TaskDTO;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Menu for selecting labels on a task.
 */
public class LabelMenu
        extends JMenu
        implements MenuListener, ListSelectionListener
{
    TaskDTO task;
    ListSelectionModel selectionModel;

    @Uses
    LabelsModel labelsModel;
    @Uses
    TaskTableModel taskModel;

    public LabelMenu()
    {
        super(i18n.text(WorkspaceResources.labels_label));
        addMenuListener(this);
        setMnemonic(KeyEvent.VK_L);
        setEnabled(false);
    }

    public void valueChanged(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting())
            return;

        selectionModel = (ListSelectionModel) e.getSource();

        if (selectionModel.isSelectionEmpty())
        {
            setEnabled(false);
        } else
        {
            setEnabled(true);
        }
    }

    public void menuSelected(MenuEvent e)
    {
        removeAll();

        if (selectionModel.isSelectionEmpty())
            return;

        TaskDTO task = taskModel.getTask(selectionModel.getMinSelectionIndex());

        if (task == null)
            return;

        List<ListItemValue> labels = task.labels().get().items().get();
        int size = labelsModel.getSize();
        for (int i = 0; i < size; i++)
        {
            final ListItemValue label = labelsModel.getElementAt(i);
            JCheckBoxMenuItem checkBoxMenuItem = new JCheckBoxMenuItem(new AbstractAction(label.description().get())
            {
                public void actionPerformed(ActionEvent e)
                {
                    JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) e.getSource();
                    for (int idx = selectionModel.getMinSelectionIndex(); idx <= selectionModel.getMaxSelectionIndex(); idx++)
                    {
                        if (selectionModel.isSelectedIndex(idx))
                        {
/*
                            if (checkBoxMenuItem.isSelected())
                            {
                                try
                                {
                                    taskModel.addLabel(idx, label);
                                } catch (ResourceException e1)
                                {
                                    e1.printStackTrace();
                                }
                            } else
                            {
                                try
                                {
                                    taskModel.removeLabel(idx, label);
                                } catch (ResourceException e1)
                                {
                                    e1.printStackTrace();
                                }
                            }
*/
                        }
                    }
                }
            });
            boolean state = false;
            for (ListItemValue labelDTO : labels)
            {
                if (labelDTO.entity().get().equals(label.entity().get()))
                    state = true;
            }
            checkBoxMenuItem.setState(state);
            add(checkBoxMenuItem);
        }
    }

    public void menuDeselected(MenuEvent e)
    {
    }

    public void menuCanceled(MenuEvent e)
    {
    }

}
