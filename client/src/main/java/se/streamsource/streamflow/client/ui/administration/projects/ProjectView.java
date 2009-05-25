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
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * JAVADOC
 */
public class ProjectView
        extends JPanel
{
    @Structure
    ObjectBuilderFactory obf;

    @Service
    DialogService dialogs;

    private ProjectModel model;
    public JXTree memberRoleTree;

    public ProjectView(@Service ActionMap am, @Service ProjectModel model)
    {
        super(new BorderLayout());
        this.model = model;

        setActionMap(am);

        memberRoleTree = new JXTree(model);
        memberRoleTree.setRootVisible(false);
        memberRoleTree.setShowsRootHandles(true);

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

        add(memberRoleTree, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("addMember")));
        toolbar.add(new JButton(am.get("removeMember")));
        toolbar.add(new JButton(am.get("addMemberRole")));
        add(toolbar, BorderLayout.SOUTH);
    }

    public JXTree getMembers()
    {
        return memberRoleTree;
    }
}