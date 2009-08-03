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

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.WrappingIconPanel;
import org.jdesktop.swingx.renderer.WrappingProvider;
import org.jdesktop.swingx.treetable.TreeTableNode;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.SearchFocus;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
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
    private JXTreeTable workspaceTree;
    private JSplitPane pane;
    private WorkspaceModel model;

    public WorkspaceView(final @Service ApplicationContext context,
                         @Uses WorkspaceModel model,
                         final @Structure ObjectBuilderFactory obf)
    {
        super(new BorderLayout());

        this.model = model;
        workspaceTree = new JXTreeTable(model);
        workspaceTree.expandAll();
        workspaceTree.setPreferredScrollableViewportSize(new Dimension(200, 400));
        workspaceTree.setRootVisible(false);
        workspaceTree.setShowsRootHandles(false);
        workspaceTree.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workspaceTree.setTableHeader(null);

        workspaceTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "expand");
        workspaceTree.getActionMap().put("expand", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                workspaceTree.expandRow(workspaceTree.getSelectedRow());
            }
        });
        workspaceTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "collapse");
        workspaceTree.getActionMap().put("collapse", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                workspaceTree.collapseRow(workspaceTree.getSelectedRow());
            }
        });

        workspaceTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "select");
        workspaceTree.getActionMap().put("select", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                pane.getRightComponent().requestFocus();
            }
        });

        workspaceTree.setTreeCellRenderer(new DefaultTreeRenderer(new WrappingProvider(
                new IconValue()
                {
                    public Icon getIcon(Object o)
                    {
                        if (o instanceof UserNode)
                            return i18n.icon(Icons.user, i18n.ICON_24);
                        else if (o instanceof ProjectsNode)
                            return i18n.icon(Icons.projects, i18n.ICON_24);
                        else if (o instanceof UserInboxNode || o instanceof ProjectInboxNode)
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

        JScrollPane workspaceScroll = new JScrollPane(workspaceTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setDividerLocation(200);
        pane.setResizeWeight(0);

        pane.setRightComponent(new JPanel());

        JPanel workspaceOutline = new JPanel(new BorderLayout());
        workspaceOutline.add(workspaceScroll, BorderLayout.CENTER);
//        workspaceOutline.add(toolbarView, BorderLayout.SOUTH);
        workspaceOutline.setMinimumSize(new Dimension(150, 300));

        pane.setLeftComponent(workspaceOutline);

        add(pane, BorderLayout.CENTER);

        workspaceTree.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                final TreePath path = e.getNewLeadSelectionPath();
                if (path != null)
                {
                    Object node = path.getLastPathComponent();

                    JComponent view = new JPanel();

                    if (node instanceof UserNode)
                        view = new JPanel();
                    else if (node instanceof ProjectsNode)
                    {
                        view = new JPanel();
                    }
                    else if (node instanceof UserInboxNode)
                    {
                        UserInboxNode userInboxNode = (UserInboxNode) node;
                        final UserInboxModel inboxModel = userInboxNode.inboxModel();
                        final LabelsModel labelsModel = userInboxNode.getParent().labelsModel();
                        view = obf.newObjectBuilder(UserInboxView.class).use(inboxModel, userInboxNode.getParent(), labelsModel).newInstance();

                        context.getTaskService().execute(new Task(context.getApplication())
                        {
                            protected Object doInBackground() throws Exception
                            {
                                try
                                {
                                    inboxModel.refresh();
                                    labelsModel.refresh();
                                } catch (ResourceException e)
                                {
                                    e.printStackTrace();
                                }

                                return null;
                            }
                        });
                    }
                    else if (node instanceof UserAssignmentsNode)
                    {
                        UserAssignmentsNode userAssignmentsNode = (UserAssignmentsNode) node;
                        final UserAssignmentsModel assignmentsModel = userAssignmentsNode.assignmentsModel();
                        final LabelsModel labelsModel = userAssignmentsNode.getParent().labelsModel();
                        view = obf.newObjectBuilder(UserAssignmentsView.class).use(assignmentsModel, userAssignmentsNode.getParent(),labelsModel).newInstance();

                        context.getTaskService().execute(new Task(context.getApplication())
                        {
                            protected Object doInBackground() throws Exception
                            {
                                try
                                {
                                    assignmentsModel.refresh();
                                    labelsModel.refresh();
                                } catch (ResourceException e)
                                {
                                    e.printStackTrace();
                                }

                                return null;
                            }
                        });
                    }
                    else if (node instanceof UserDelegationsNode)
                    {
                        UserDelegationsNode userDelegationsNode = (UserDelegationsNode) node;
                        final UserDelegationsModel delegationsModel = userDelegationsNode.delegationsModel();
                        final LabelsModel labelsModel = userDelegationsNode.getParent().labelsModel();
                        view = obf.newObjectBuilder(UserDelegationsView.class).use(delegationsModel, labelsModel).newInstance();

                        context.getTaskService().execute(new Task(context.getApplication())
                        {
                            protected Object doInBackground() throws Exception
                            {
                                try
                                {
                                    delegationsModel.refresh();
                                    labelsModel.refresh();
                                } catch (ResourceException e)
                                {
                                    e.printStackTrace();
                                }

                                return null;
                            }
                        });
                    }
                    else if (node instanceof UserWaitingForNode)
                    {
                        UserWaitingForNode userWaitingForNode = (UserWaitingForNode) node;
                        final UserWaitingForModel waitingForModel = userWaitingForNode.waitingForModel();
                        final LabelsModel labelsModel = userWaitingForNode.getParent().labelsModel();
                        view = obf.newObjectBuilder(UserWaitingForView.class).use(waitingForModel, labelsModel).newInstance();

                        context.getTaskService().execute(new Task(context.getApplication())
                        {
                            protected Object doInBackground() throws Exception
                            {
                                try
                                {
                                    waitingForModel.refresh();
                                    labelsModel.refresh();
                                } catch (ResourceException e)
                                {
                                    e.printStackTrace();
                                }

                                return null;
                            }
                        });
                    }
                    else if (node instanceof ProjectInboxNode)
                    {

                    }
                    else if (node instanceof ProjectAssignmentsNode)
                    {

                    }
                    else if (node instanceof ProjectDelegationsNode)
                    {

                    }
                    else if (node instanceof ProjectWaitingForNode)
                    {

                    }

                    pane.setRightComponent(view);
                } else
                {
                    pane.setRightComponent(new JPanel());
                }
            }
        });

        workspaceTree.addFocusListener(obf.newObjectBuilder(SearchFocus.class).use(workspaceTree.getSearchable()).newInstance());
    }

    public JXTreeTable getWorkspaceTree()
    {
        return workspaceTree;
    }

    public String getSelectedUser()
    {
        return model.getRoot().getUserObject().settings().userName().get();
    }

    public JSplitPane getPane()
    {
        return pane;
    }

    public void refreshTree()
    {
        workspaceTree.clearSelection();
        model.refresh();
        workspaceTree.expandAll();
    }

}
