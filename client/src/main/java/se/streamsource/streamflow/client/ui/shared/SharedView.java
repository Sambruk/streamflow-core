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

package se.streamsource.streamflow.client.ui.shared;

import org.jdesktop.swingx.JXTreeTable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.SearchFocus;
import se.streamsource.streamflow.client.ui.DetailView;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * JAVADOC
 */
public class SharedView
        extends JPanel
{
    // SharedInbox listing
    private JXTreeTable sharedTree;
    private JSplitPane pane;

    public SharedView(@Service ActionMap am,
                      @Service SharedModel model,
                      @Structure ObjectBuilderFactory obf)
    {
        super(new BorderLayout());

        sharedTree = new JXTreeTable(model);
        sharedTree.setPreferredScrollableViewportSize(new Dimension(300, 400));
        sharedTree.setRootVisible(false);
        sharedTree.setShowsRootHandles(false);
        sharedTree.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sharedTree.setTableHeader(null);

        sharedTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "expand");
        sharedTree.getActionMap().put("expand", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                sharedTree.expandRow(sharedTree.getSelectedRow());
            }
        });
        sharedTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "collapse");
        sharedTree.getActionMap().put("collapse", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                sharedTree.collapseRow(sharedTree.getSelectedRow());
            }
        });

        sharedTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "select");
        sharedTree.getActionMap().put("select", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                pane.getRightComponent().requestFocus();
            }
        });
        //sharedTree

        JScrollPane sharedScroll = new JScrollPane(sharedTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setDividerLocation(200);
        pane.setResizeWeight(0);

        pane.setRightComponent(new JPanel());

        JPanel sharedOutline = new JPanel(new BorderLayout());
        sharedOutline.add(sharedScroll, BorderLayout.CENTER);
//        sharedOutline.add(toolbarView, BorderLayout.SOUTH);
        sharedOutline.setMinimumSize(new Dimension(150, 400));

        pane.setLeftComponent(sharedOutline);

        add(pane, BorderLayout.CENTER);

        sharedTree.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                TreePath path = e.getPath();
                if (path != null && path.getLastPathComponent() instanceof DetailView)
                {
                    DetailView view = (DetailView) path.getLastPathComponent();
                    try
                    {
                        pane.setRightComponent(view.detailView());
                    } catch (ResourceException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            }
        });

        sharedTree.addFocusListener(obf.newObjectBuilder(SearchFocus.class).use(sharedTree.getSearchable()).newInstance());
    }

    public JXTreeTable getSharedTree()
    {
        return sharedTree;
    }

    public String getSelectedUser()
    {
        int selected = sharedTree.getSelectedRow();
        if (selected == -1)
            return null;

        Object selectedNode = sharedTree.getPathForRow(selected).getPathComponent(3);
        if (selectedNode instanceof SharedUserInboxNode)
        {
            SharedUserInboxNode userInboxNode = (SharedUserInboxNode) selectedNode;
            return userInboxNode.getSettings().userName().get();
        } else if (selectedNode instanceof SharedUserDelegationsNode)
        {
            SharedUserDelegationsNode userDelegationsNode = (SharedUserDelegationsNode) selectedNode;
            return userDelegationsNode.getSettings().userName().get();
        } else if (selectedNode instanceof SharedUserAssignmentsNode)
        {
            SharedUserAssignmentsNode assignmentsNode = (SharedUserAssignmentsNode) selectedNode;
            return assignmentsNode.getSettings().userName().get();
        } else
        {
            SharedUserWaitingForNode waitingForNode = (SharedUserWaitingForNode) selectedNode;
            return waitingForNode.getSettings().userName().get();
        }
    }

    public JSplitPane getPane()
    {
        return pane;
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        sharedTree.expandAll();
    }

}
