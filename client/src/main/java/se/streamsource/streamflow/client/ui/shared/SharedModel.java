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

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.qi4j.api.injection.scope.Uses;

/**
 * JAVADOC
 */
public class SharedModel
        extends DefaultTreeTableModel
{
    public SharedModel(@Uses SharedNode root)
    {
        super(root);

/*
        // Add top nodes
        DefaultMutableTreeTableNode me = (DefaultMutableTreeTableNode) getRoot();
        model.insertNodeInto(me, root, root.getChildCount());
        final AllInboxesNode inboxes = obf.newObject(AllInboxesNode.class);
        model.insertNodeInto(inboxes, me, me.getChildCount());

        individualRepository.individual().visitAccounts(new AccountVisitor()
        {
            public void visitAccount(Account account)
            {
                model.insertNodeInto(obf.newObjectBuilder(InboxNode.class).use(account).newInstance(), inboxes, inboxes.getChildCount());
            }
        });

        model.insertNodeInto(new DefaultMutableTreeTableNode("Delegations"), me, me.getChildCount());
        model.insertNodeInto(new DefaultMutableTreeTableNode("Assignments"), me, me.getChildCount());
        model.insertNodeInto(new DefaultMutableTreeTableNode("Waiting for"), me, me.getChildCount());

        DefaultMutableTreeTableNode project = new DefaultMutableTreeTableNode("SharedProjects");
        model.insertNodeInto(project, root, root.getChildCount());
        model.insertNodeInto(new DefaultMutableTreeTableNode("Inboxes"), project, project.getChildCount());
        DefaultMutableTreeTableNode projectDelegations = new DefaultMutableTreeTableNode("Delegations");
        model.insertNodeInto(projectDelegations, project, project.getChildCount());
        model.insertNodeInto(new DefaultMutableTreeTableNode("StreamFlow"), projectDelegations, projectDelegations.getChildCount());
        DefaultMutableTreeTableNode projectAssign = new DefaultMutableTreeTableNode("Assignments");
        model.insertNodeInto(projectAssign, project, project.getChildCount());
        model.insertNodeInto(new DefaultMutableTreeTableNode("StreamFlow"), projectAssign, projectAssign.getChildCount());
        DefaultMutableTreeTableNode projectWaiting = new DefaultMutableTreeTableNode("Waiting for");
        model.insertNodeInto(projectWaiting, project, project.getChildCount());
        model.insertNodeInto(new DefaultMutableTreeTableNode("StreamFlow"), projectWaiting, projectWaiting.getChildCount());
*/
    }

    @Override
    public int getColumnCount()
    {
        return 1;
    }

    @Override
    public Class<?> getColumnClass(int column)
    {
        switch (column)
        {
            case 0:
                return String.class;
        }
        return super.getColumnClass(column);
    }

    @Override
    public boolean isCellEditable(Object o, int i)
    {
        return false;
    }
}
