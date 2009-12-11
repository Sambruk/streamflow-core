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

package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.infrastructure.ui.FilteredList;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class TaskTypesDialog
    extends JPanel
{
    private FilteredList list;

    private ListItemValue selected;

    public TaskTypesDialog(@Service ApplicationContext context)
    {
        setLayout(new BorderLayout());
        setActionMap(context.getActionMap(this));

        list = new FilteredList();

        add(list, BorderLayout.CENTER);
    }

    public JList getTaskTypeSelector()
    {
        return list.getList();
    }

    public void setModel( PossibleTaskTypesModel model)
    {
        model.refresh();
        list.setEventList( model.getTaskTypeList() );
    }

    public EntityReference getSelected()
    {
        return selected == null ? null : selected.entity().get();
    }

    @Action
    public void execute()
    {
        selected = (ListItemValue) list.getList().getSelectedValue();

        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}
