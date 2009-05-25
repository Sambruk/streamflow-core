/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * JAVADOC
 */
public class ContextsView
        extends JPanel
{
    private JXTreeTable contextTree;
    private JSplitPane pane;

    public ContextsView()
    {
        super(new BorderLayout());

        setMinimumSize(new Dimension(200, 0));

        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode("#contexts");
        DefaultMutableTreeTableNode phone = new DefaultMutableTreeTableNode("@Phone");
        DefaultMutableTreeTableNode phoneHome = new DefaultMutableTreeTableNode("@Home");
        DefaultMutableTreeTableNode phoneWork = new DefaultMutableTreeTableNode("@Work");
        phone.add(phoneHome);
        phone.add(phoneWork);
        DefaultMutableTreeTableNode computer = new DefaultMutableTreeTableNode("@Computer");
        DefaultMutableTreeTableNode meeting = new DefaultMutableTreeTableNode("@Staff meeting");
        root.add(phone);
        root.add(computer);
        root.add(meeting);
        DefaultTreeTableModel model = new DefaultTreeTableModel(root);
        model.setColumnIdentifiers(Arrays.asList(new String[]{"#name"}));
        contextTree = new JXTreeTable(model);
        contextTree.setShowsRootHandles(false);
        JScrollPane projectScroll = new JScrollPane(contextTree);
        add(projectScroll, BorderLayout.CENTER);

        JScrollPane contextsScroll = new JScrollPane(contextTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setDividerLocation(300);
        pane.setResizeWeight(0);

        pane.setRightComponent(new JPanel());

        JPanel contextsOutline = new JPanel(new BorderLayout());
        contextsOutline.add(contextsScroll, BorderLayout.CENTER);
        contextsOutline.setMinimumSize(new Dimension(300, 400));

        pane.setLeftComponent(contextsOutline);

        add(pane, BorderLayout.CENTER);


    }
}