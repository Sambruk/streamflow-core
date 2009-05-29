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

import org.jdesktop.swingx.JXTree;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationModel;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;

/**
 * JAVADOC
 */
public class ProjectView
        extends JSplitPane
{
    public JXTree memberRoleTree;

    public ProjectView(@Service ActionMap am,
                       @Service final ProjectModel model,
                       @Service final MemberRolesModel memberRolesModel,
                       @Service final MemberRolesView memberRolesView)
    {
        super();
        JPanel projectView = new JPanel(new BorderLayout());

        setActionMap(am);

        memberRoleTree = new JXTree(model);
        memberRoleTree.setRootVisible(false);
        memberRoleTree.setShowsRootHandles(true);
        memberRoleTree.addTreeSelectionListener(new TreeSelectionListener() {

                public void valueChanged(TreeSelectionEvent treeSelectionEvent)
                {
                    TreePath newPath = treeSelectionEvent.getNewLeadSelectionPath();
                    if (newPath == null)
                    {
                        memberRolesModel.clear();
                    } else
                    {
                        TreeNodeValue value = (TreeNodeValue) newPath.getPathComponent(1);
                        memberRolesModel.setMember(model.getProject().members().member(value.entity().get().identity()));
                        setRightComponent(memberRolesView);
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

        projectView.add(memberRoleTree, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("addMember")));
        toolbar.add(new JButton(am.get("removeMember")));
        projectView.add(toolbar, BorderLayout.SOUTH);

        setLeftComponent(projectView);
        setRightComponent(memberRolesView);
    }

    public JXTree getMembers()
    {
        return memberRoleTree;
    }
}