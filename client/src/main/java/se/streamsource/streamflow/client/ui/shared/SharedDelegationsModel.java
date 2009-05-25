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
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.domain.individual.Account;

/**
 * JAVADOC
 */
public class SharedDelegationsModel
        extends DefaultTreeTableModel
{
    @Structure
    ValueBuilderFactory vbf;

    Account account;

    public SharedDelegationsModel()
    {
        super(new DefaultMutableTreeTableNode());
    }


    @Override
    public int getColumnCount()
    {
        return 3;
    }

    @Override
    public boolean isCellEditable(Object o, int i)
    {
        return true;
    }

    @Override
    public Object getValueAt(Object node, int column)
    {
        return super.getValueAt(node, column);
    }

    @Override
    public void setValueAt(Object value, Object node, int column)
    {
        switch (column)
        {
            case 1:
            {
                Boolean completed = (Boolean) value;
                if (completed)
                {
/*
                    DefaultTreeTableNode treeNode = (DefaultTreeTableNode) node;
                    SharedTask task = (SharedTask) treeNode.getUserObject();
                    ValueBuilder<CompleteInboxTaskCommand> contextBuilder = vbf.newValueBuilder(CompleteInboxTaskCommand.class);
                    contextBuilder.prototype().context().set(inbox());
                    contextBuilder.prototype().sharedTask().set(task);
                    try
                    {
                        interactions.completeSharedTask(contextBuilder.newInstance());
                    } catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
*/
                }

            }
        }

        return; // Skip if don't know what is going on
    }

    public void setAccount(Account account)
    {
        this.account = account;
    }

    public Account getAccount()
    {

        return account;
    }
}