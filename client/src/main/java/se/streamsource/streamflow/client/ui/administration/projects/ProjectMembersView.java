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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTree;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.ui.administration.SelectUsersAndGroupsDialog;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Set;

/**
 * JAVADOC
 */
public class ProjectMembersView
        extends JSplitPane
{
    @Service
    DialogService dialogs;

    @Uses
    Iterable<SelectUsersAndGroupsDialog> selectUsersAndGroups;

    @Structure
    ObjectBuilderFactory obf;

    public JXTree memberRoleTree;
    private ProjectMembersModel membersModel;

    public ProjectMembersView(@Service ApplicationContext context,
                              @Uses final ProjectMembersModel membersModel)
    {
        super();
        this.membersModel = membersModel;
        JPanel members = new JPanel(new BorderLayout());

        setActionMap(context.getActionMap(this));

        memberRoleTree = new JXTree(membersModel);
        memberRoleTree.setRootVisible(false);
        memberRoleTree.setShowsRootHandles(true);
        memberRoleTree.addTreeSelectionListener(new TreeSelectionListener()
        {

            public void valueChanged(TreeSelectionEvent treeSelectionEvent)
            {
                TreePath newPath = treeSelectionEvent.getNewLeadSelectionPath();
                if (newPath == null)
                {
                    setRightComponent(new JPanel());
                } else
                {
                    TreeNodeValue value = (TreeNodeValue) newPath.getPathComponent(1);
                    MemberRolesModel memberRolesModel = membersModel.memberRolesModel(value.entity().get().identity());
                    memberRolesModel.refresh();
                    setRightComponent(obf.newObjectBuilder(MemberRolesView.class).use(memberRolesModel).newInstance());
                }
            }
        }
        );

        memberRoleTree.setCellRenderer(new DefaultTreeCellRenderer()
        {
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
            {
                if (value instanceof TreeNodeValue)
                {
                    TreeNodeValue node = (TreeNodeValue) value;
                    return super.getTreeCellRendererComponent(tree, node.description().get(), selected, expanded, leaf, row, hasFocus);
                } else
                {
                    return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                }
            }
        });

        members.add(memberRoleTree, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(getActionMap().get("add")));
        toolbar.add(new JButton(getActionMap().get("remove")));
        members.add(toolbar, BorderLayout.SOUTH);

        setLeftComponent(members);
        setRightComponent(new JPanel());
    }


    @Action
    public void add()
    {
        SelectUsersAndGroupsDialog dialog = selectUsersAndGroups.iterator().next();
        dialogs.showOkCancelHelpDialog(this, dialog);
        Set<String> members = dialog.getUsersAndGroups();
        if (members != null)
        {
            membersModel.addMembers(members);
        }
    }

    @Action
    public void remove()
    {
        if (memberRoleTree.getSelectionPath() != null)
        {
            TreeNodeValue selected = (TreeNodeValue) memberRoleTree.getSelectionPath().getPathComponent(1);
            membersModel.removeMember(selected.entity().get());
        }
    }
}