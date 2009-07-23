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
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.WrappingIconPanel;
import org.jdesktop.swingx.renderer.WrappingProvider;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.SearchFocus;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.DetailView;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * JAVADOC
 */
public class WorkspaceView
        extends JPanel
{
    // SharedInbox listing
    private JXTreeTable sharedTree;
    private JSplitPane pane;
    private WorkspaceModel model;

    public WorkspaceView(@Service ActionMap am,
                      @Service WorkspaceModel model,
                      @Structure ObjectBuilderFactory obf)
    {
        super(new BorderLayout());

        this.model = model;
        sharedTree = new JXTreeTable(model);
        sharedTree.setPreferredScrollableViewportSize(new Dimension(200, 400));
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

        sharedTree.setTreeCellRenderer(new DefaultTreeRenderer(new WrappingProvider(
                new IconValue()
                {
                    public Icon getIcon(Object o)
                    {
                        if (o instanceof UserNode)
                            return i18n.icon(Icons.user, i18n.ICON_24);
                        else if (o instanceof ProjectsNode)
                            return i18n.icon(Icons.projects, i18n.ICON_24);
                        if (o instanceof UserInboxNode || o instanceof ProjectInboxNode)
                            return i18n.icon(Icons.inbox, i18n.ICON_16);
                        else if (o instanceof UserAssignmentsNode || o instanceof ProjectAssignmentsNode)
                            return i18n.icon(Icons.assign, i18n.ICON_16);
                        else if (o instanceof UserDelegationsNode || o instanceof ProjectDelegationsNode)
                            return i18n.icon(Icons.delegate, i18n.ICON_16);
                        else if (o instanceof UserWaitingForNode || o instanceof ProjectWaitingForNode)
                            return i18n.icon(Icons.waitingfor, i18n.ICON_16);
                        else
                            return NULL_ICON;
                    }
                },
                new StringValue()
                {
                    public String getString(Object o)
                    {
                        return ((TreeTableNode) o).getValueAt(0).toString();
                    }
                },
                false
        ))
        {
            @Override
            public Component getTreeCellRendererComponent(JTree jTree, Object o, boolean b, boolean b1, boolean b2, int i, boolean b3)
            {
                WrappingIconPanel component = (WrappingIconPanel) super.getTreeCellRendererComponent(jTree, o, b, b1, b2, i, b3);

                if (o instanceof UserNode || o instanceof ProjectsNode)
                {
                    component.setFont(getFont().deriveFont(Font.BOLD));
                } else
                {
                    component.setFont(getFont().deriveFont(Font.PLAIN));
                }


                return component;
            }
        });

        JScrollPane sharedScroll = new JScrollPane(sharedTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setDividerLocation(200);
        pane.setResizeWeight(0);

        pane.setRightComponent(new JPanel());

        JPanel sharedOutline = new JPanel(new BorderLayout());
        sharedOutline.add(sharedScroll, BorderLayout.CENTER);
//        sharedOutline.add(toolbarView, BorderLayout.SOUTH);
        sharedOutline.setMinimumSize(new Dimension(150, 300));

        pane.setLeftComponent(sharedOutline);

        add(pane, BorderLayout.CENTER);

        sharedTree.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                final TreePath path = e.getNewLeadSelectionPath();
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
                } else
                {
                    pane.setRightComponent(new JPanel());
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
        if (selectedNode instanceof UserInboxNode)
        {
            UserInboxNode userInboxNode = (UserInboxNode) selectedNode;
            return userInboxNode.getSettings().userName().get();
        } else if (selectedNode instanceof UserDelegationsNode)
        {
            UserDelegationsNode userDelegationsNode = (UserDelegationsNode) selectedNode;
            return userDelegationsNode.getSettings().userName().get();
        } else if (selectedNode instanceof UserAssignmentsNode)
        {
            UserAssignmentsNode assignmentsNode = (UserAssignmentsNode) selectedNode;
            return assignmentsNode.getSettings().userName().get();
        } else if (selectedNode instanceof UserWaitingForNode)
        {
            UserWaitingForNode waitingForNode = (UserWaitingForNode) selectedNode;
            return waitingForNode.getSettings().userName().get();
        } else if (selectedNode instanceof ProjectNode)
        {
            ProjectNode projectNode = (ProjectNode) selectedNode;
            return projectNode.getSettings().userName().get();
        }
        // warning?
        return "";
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

    public void refreshTree()
    {
        sharedTree.clearSelection();
        model.refresh();
        sharedTree.expandAll();
    }

}
