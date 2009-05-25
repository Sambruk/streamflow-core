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

package se.streamsource.streamflow.client.ui.shared;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.domain.roles.DetailView;
import se.streamsource.streamflow.resource.inbox.InboxTaskValue;

import javax.swing.JComponent;

/**
 * JAVADOC
 */
public class InboxTaskNode
        extends DefaultMutableTreeTableNode
        implements DetailView
{
    @Uses
    SharedTaskView view;

    @Uses
    SharedTaskModel model;

    public InboxTaskNode(@Uses InboxTaskValue taskValue)
    {
        super(taskValue);
    }

    public JComponent detailView()
    {
        return view;
    }

    @Override
    public int getColumnCount()
    {
        return 3;
    }

    @Override
    public Object getValueAt(int column)
    {
        InboxTaskValue value = (InboxTaskValue) getUserObject();
        switch (column)
        {
            case 0:
                return value.description().get();
            case 1:
                return value.creationDate().get();
            case 2:
                return false;
        }

        return null;
    }
}