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

package se.streamsource.streamflow.client.ui.overview;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.*;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.SearchFocus;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AccountModel;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * JAVADOC
 */
public class OverviewView
        extends JPanel
{
    private JXTree overviewTree;
    private JSplitPane pane;
    private OverviewModel model;

    public OverviewView(final @Service ApplicationContext context,
                        @Uses final OverviewModel model,
                        @Uses final AccountModel accountModel,
                        final @Structure ObjectBuilderFactory obf)
    {
        super(new BorderLayout());

        this.model = model;
        overviewTree = new JXTree(model);
        overviewTree.expandAll();
        overviewTree.setRootVisible(false);
        overviewTree.setShowsRootHandles(false);
        overviewTree.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        overviewTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "select");
        overviewTree.getActionMap().put("select", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                pane.getRightComponent().requestFocus();
            }
        });

        overviewTree.setCellRenderer(new DefaultTreeRenderer(new WrappingProvider(
                new IconValue()
                {
                    public Icon getIcon(Object o)
                    {
                        if (o instanceof OverviewProjectNode)
                            return i18n.icon(Icons.project, i18n.ICON_24);
                        else if (o instanceof OverviewProjectsNode)
                            return i18n.icon(Icons.projects, i18n.ICON_24);
                        else if (o instanceof OverviewProjectAssignmentsNode)
                            return i18n.icon(Icons.assign, i18n.ICON_16);
                        else if (o instanceof OverviewProjectWaitingForNode)
                            return i18n.icon(Icons.waitingfor, i18n.ICON_16);
                        else
                            return NULL_ICON;
                    }
                },
                new StringValue()
                {
                    public String getString(Object o)
                    {
                        if (o instanceof OverviewProjectNode)
                            return ((OverviewProjectNode) o).projectName();
                        else if (o instanceof OverviewProjectsNode)
                            return i18n.text(OverviewResources.projects_node);
                        else if (o instanceof OverviewProjectAssignmentsNode)
                            return o.toString();
                        else if (o instanceof OverviewProjectWaitingForNode)
                            return o.toString();
                        else
                            return "";
                    }
                },
                false
        ))
        {
            @Override
            public Component getTreeCellRendererComponent(JTree jTree, Object o, boolean b, boolean b1, boolean b2, int i, boolean b3)
            {
                WrappingIconPanel component = (WrappingIconPanel) super.getTreeCellRendererComponent(jTree, o, b, b1, b2, i, b3);

                if (o instanceof OverviewProjectsNode)
                {
                    component.setFont(getFont().deriveFont(Font.BOLD));
                } else
                {
                    component.setFont(getFont().deriveFont(Font.PLAIN));
                }


                return component;
            }
        });

        JScrollPane workspaceScroll = new JScrollPane(overviewTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setDividerLocation(200);
        pane.setResizeWeight(0);

        pane.setRightComponent(new JPanel());

        JPanel overviewOutline = new JPanel(new BorderLayout());
        overviewOutline.add(workspaceScroll, BorderLayout.CENTER);
        overviewOutline.setMinimumSize(new Dimension(150, 300));

        pane.setLeftComponent(overviewOutline);

        add(pane, BorderLayout.CENTER);

        overviewTree.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent e)
            {
                final TreePath path = e.getNewLeadSelectionPath();
                if (path != null)
                {
                    Object node = path.getLastPathComponent();

                    JComponent view = new JPanel();

                    if (node instanceof OverviewProjectsNode)
                    {
                        view = new JPanel();
                    } else if (node instanceof OverviewProjectAssignmentsNode)
                    {
                        OverviewProjectAssignmentsNode projectAssignmentsNode = (OverviewProjectAssignmentsNode) node;
                        final OverviewProjectAssignmentsModel assignmentsModel = projectAssignmentsNode.assignmentsModel();
                        view = obf.newObjectBuilder(OverviewProjectAssignmentsView.class).use(assignmentsModel, projectAssignmentsNode.getParent()).newInstance();

                        context.getTaskService().execute(new Task(context.getApplication())
                        {
                            protected Object doInBackground() throws Exception
                            {
                                try
                                {
                                    assignmentsModel.refresh();
                                } catch (ResourceException e)
                                {
                                    e.printStackTrace();
                                }

                                return null;
                            }
                        });

                    }
/*
                    else if (node instanceof OverviewProjectWaitingForNode)
                    {
                        WorkspaceProjectWaitingForNode projectWaitingForNode = (WorkspaceProjectWaitingForNode) node;
                        final WorkspaceProjectWaitingForModel waitingForModel = projectWaitingForNode.waitingForModel();
                        final LabelsModel labelsModel = projectWaitingForNode.getParent().labelsModel();
                        view = obf.newObjectBuilder(WorkspaceProjectWaitingForView.class).use(waitingForModel, labelsModel).newInstance();

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
*/

                    pane.setRightComponent(view);
                } else
                {
                    pane.setRightComponent(new JPanel());
                }
            }
        });

        overviewTree.addFocusListener(obf.newObjectBuilder(SearchFocus.class).use(overviewTree.getSearchable()).newInstance());

        overviewTree.addTreeWillExpandListener(new TreeWillExpandListener()
        {
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException
            {
                Object node = event.getPath().getLastPathComponent();
                if (node instanceof OverviewProjectsNode)
                {
                    try
                    {
                        ((OverviewProjectsNode) node).refresh();
                        model.reload((TreeNode) node);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException
            {
            }
        });

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
        try
        {
            model.getRoot().getProjectsNode().refresh();
            model.reload(model.getRoot().getProjectsNode());
            overviewTree.clearSelection();
            overviewTree.expandAll();
        } catch (Exception e)
        {
            throw new OperationException(OverviewResources.could_not_refresh_projects, e);
        }
    }

}