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

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import se.streamsource.streamflow.domain.roles.DetailView;

import javax.swing.JComponent;

/**
 * JAVADOC
 */
public class AllDelegationsNode
        extends DefaultMutableTreeTableNode
        implements DetailView
{
    public JComponent detailView()
    {
        return new JXTable();
    }

    @Override
    public Object getValueAt(int column)
    {
        if (column == 0)
            return "Delegations";
        else
            return null;
    }
}