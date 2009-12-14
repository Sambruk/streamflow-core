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

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.WrappingIconPanel;
import org.jdesktop.swingx.renderer.WrappingProvider;
import org.jdesktop.swingx.search.SearchFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.task.TaskTableView;
import se.streamsource.streamflow.client.ui.task.TasksModel;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * JAVADOC
 */
public class WorkspaceView
      extends JPanel
{
   private JXTree workspaceTree;
   private JSplitPane pane;
   private WorkspaceModel model;
   public String accountName = "";

   public WorkspaceView( final @Service ApplicationContext context,
                         final @Structure ObjectBuilderFactory obf )
   {
      setLayout( new BorderLayout() );

      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), "selectTree" );
      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), "selectTable" );
      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), "selectDetails" );
      setActionMap( context.getActionMap( this ) );


      this.model = model;
      workspaceTree = new JXTree()
      {
         protected void doFind()
         {
            SearchFactory.getInstance().showFindBar( this, getSearchable() );
         }
      };
      workspaceTree.expandAll();
      workspaceTree.setRootVisible( false );
      workspaceTree.setShowsRootHandles( false );
      workspaceTree.getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

      workspaceTree.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), "select" );
      workspaceTree.getActionMap().put( "select", new AbstractAction()
      {
         public void actionPerformed( ActionEvent e )
         {
            pane.getRightComponent().requestFocus();
         }
      } );

      workspaceTree.setCellRenderer( new DefaultTreeRenderer( new WrappingProvider(
            new IconValue()
            {
               public Icon getIcon( Object o )
               {
                  if (o instanceof WorkspaceUserNode)
                     return i18n.icon( Icons.user, i18n.ICON_24 );
                  else if (o instanceof WorkspaceProjectNode)
                     return i18n.icon( Icons.project, i18n.ICON_24 );
                  else if (o instanceof WorkspaceProjectsNode)
                     return i18n.icon( Icons.projects, i18n.ICON_24 );
                  else if (o instanceof WorkspaceUserInboxNode || o instanceof WorkspaceProjectInboxNode)
                     return i18n.icon( Icons.inbox, i18n.ICON_16 );
                  else if (o instanceof WorkspaceUserAssignmentsNode || o instanceof WorkspaceProjectAssignmentsNode)
                     return i18n.icon( Icons.assign, i18n.ICON_16 );
                  else if (o instanceof WorkspaceUserDelegationsNode || o instanceof WorkspaceProjectDelegationsNode)
                     return i18n.icon( Icons.delegate, i18n.ICON_16 );
                  else if (o instanceof WorkspaceUserWaitingForNode || o instanceof WorkspaceProjectWaitingForNode)
                     return i18n.icon( Icons.waitingfor, i18n.ICON_16 );
                  else
                     return NULL_ICON;
               }
            },
            new StringValue()
            {
               public String getString( Object o )
               {
                  if (o instanceof WorkspaceUserNode)
                     return accountName;
                  else if (o instanceof WorkspaceProjectNode)
                     return ((WorkspaceProjectNode) o).projectName();
                  else if (o instanceof WorkspaceProjectsNode)
                     return i18n.text( WorkspaceResources.projects_node );
                  else if (o instanceof WorkspaceUserInboxNode || o instanceof WorkspaceProjectInboxNode)
                     return o.toString();
                  else if (o instanceof WorkspaceUserAssignmentsNode || o instanceof WorkspaceProjectAssignmentsNode)
                     return o.toString();
                  else if (o instanceof WorkspaceUserDelegationsNode || o instanceof WorkspaceProjectDelegationsNode)
                     return o.toString();
                  else if (o instanceof WorkspaceUserWaitingForNode || o instanceof WorkspaceProjectWaitingForNode)
                     return o.toString();
                  else
                     return "";
               }
            },
            false
      ) )
      {
         @Override
         public Component getTreeCellRendererComponent( JTree jTree, Object o, boolean b, boolean b1, boolean b2, int i, boolean b3 )
         {
            WrappingIconPanel component = (WrappingIconPanel) super.getTreeCellRendererComponent( jTree, o, b, b1, b2, i, b3 );

            if (o instanceof WorkspaceUserNode || o instanceof WorkspaceProjectsNode)
            {
               component.setFont( getFont().deriveFont( Font.BOLD ) );
            } else
            {
               component.setFont( getFont().deriveFont( Font.PLAIN ) );
            }


            return component;
         }
      } );

      JScrollPane workspaceScroll = new JScrollPane( workspaceTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );

      pane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
      pane.setDividerLocation( 200 );
      pane.setResizeWeight( 0 );

      pane.setRightComponent( new JPanel() );

      JPanel workspaceOutline = new JPanel( new BorderLayout() );
      workspaceOutline.add( workspaceScroll, BorderLayout.CENTER );
      workspaceOutline.setMinimumSize( new Dimension( 150, 300 ) );

      pane.setLeftComponent( workspaceOutline );

      add( pane, BorderLayout.CENTER );

      workspaceTree.addTreeSelectionListener( new TreeSelectionListener()
      {
         public void valueChanged( TreeSelectionEvent e )
         {
            final TreePath path = e.getNewLeadSelectionPath();
            if (path != null)
            {
               Object node = path.getLastPathComponent();

               JComponent view = new JPanel();

               if (node instanceof WorkspaceUserNode)
                  view = new JPanel();
               else if (node instanceof WorkspaceProjectsNode)
               {
                  view = new JPanel();
               } else if (node instanceof WorkspaceUserInboxNode)
               {
                  WorkspaceUserInboxNode userInboxNode = (WorkspaceUserInboxNode) node;
                  final WorkspaceUserInboxModel inboxModel = userInboxNode.inboxModel();
                  TasksModel tasksModel = model.getRoot().getUserObject().tasks();
                  view = obf.newObjectBuilder( WorkspaceUserInboxView.class ).use( inboxModel,
                        userInboxNode.getParent(),
                        tasksModel ).newInstance();

                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        inboxModel.refresh();

                        return null;
                     }
                  } );
               } else if (node instanceof WorkspaceUserAssignmentsNode)
               {
                  WorkspaceUserAssignmentsNode userAssignmentsNode = (WorkspaceUserAssignmentsNode) node;
                  final WorkspaceUserAssignmentsModel assignmentsModel = userAssignmentsNode.assignmentsModel();
                  TasksModel tasksModel = model.getRoot().getUserObject().tasks();
                  view = obf.newObjectBuilder( WorkspaceUserAssignmentsView.class ).use( assignmentsModel,
                        userAssignmentsNode.getParent(),
                        tasksModel ).newInstance();

                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        assignmentsModel.refresh();

                        return null;
                     }
                  } );
               } else if (node instanceof WorkspaceUserDelegationsNode)
               {
                  WorkspaceUserDelegationsNode userDelegationsNode = (WorkspaceUserDelegationsNode) node;
                  final WorkspaceUserDelegationsModel delegationsModel = userDelegationsNode.delegationsModel();
                  TasksModel tasksModel = model.getRoot().getUserObject().tasks();
                  view = obf.newObjectBuilder( WorkspaceUserDelegationsView.class ).use( delegationsModel,
                        userDelegationsNode.getParent(),
                        tasksModel ).newInstance();

                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        delegationsModel.refresh();

                        return null;
                     }
                  } );
               } else if (node instanceof WorkspaceUserWaitingForNode)
               {
                  WorkspaceUserWaitingForNode userWaitingForNode = (WorkspaceUserWaitingForNode) node;
                  final WorkspaceUserWaitingForModel waitingForModel = userWaitingForNode.waitingForModel();
                  TasksModel tasksModel = model.getRoot().getUserObject().tasks();
                  view = obf.newObjectBuilder( WorkspaceUserWaitingForView.class ).use( waitingForModel,
                        userWaitingForNode.getParent(),
                        tasksModel ).newInstance();

                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        waitingForModel.refresh();

                        return null;
                     }
                  } );
               } else if (node instanceof WorkspaceProjectInboxNode)
               {
                  WorkspaceProjectInboxNode projectInboxNode = (WorkspaceProjectInboxNode) node;
                  final WorkspaceProjectInboxModel inboxModel = projectInboxNode.inboxModel();
                  TasksModel tasksModel = model.getRoot().getUserObject().tasks();
                  view = obf.newObjectBuilder( WorkspaceProjectInboxView.class ).use( inboxModel,
                        projectInboxNode.getParent(),
                        tasksModel ).newInstance();

                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        inboxModel.refresh();

                        return null;
                     }
                  } );
               } else if (node instanceof WorkspaceProjectAssignmentsNode)
               {
                  WorkspaceProjectAssignmentsNode projectAssignmentsNode = (WorkspaceProjectAssignmentsNode) node;
                  final WorkspaceProjectAssignmentsModel assignmentsModel = projectAssignmentsNode.assignmentsModel();
                  TasksModel tasksModel = model.getRoot().getUserObject().tasks();
                  view = obf.newObjectBuilder( WorkspaceProjectAssignmentsView.class ).use( assignmentsModel,
                        projectAssignmentsNode.getParent(),
                        tasksModel ).newInstance();

                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        assignmentsModel.refresh();

                        return null;
                     }
                  } );

               } else if (node instanceof WorkspaceProjectDelegationsNode)
               {
                  WorkspaceProjectDelegationsNode projectDelegationsNode = (WorkspaceProjectDelegationsNode) node;
                  final WorkspaceProjectDelegationsModel delegationsModel = projectDelegationsNode.delegationsModel();
                  TasksModel tasksModel = model.getRoot().getUserObject().tasks();
                  view = obf.newObjectBuilder( WorkspaceProjectDelegationsView.class ).use( delegationsModel,
                        projectDelegationsNode.getParent(),
                        tasksModel ).newInstance();

                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        delegationsModel.refresh();

                        return null;
                     }
                  } );
               } else if (node instanceof WorkspaceProjectWaitingForNode)
               {
                  WorkspaceProjectWaitingForNode projectWaitingForNode = (WorkspaceProjectWaitingForNode) node;
                  final WorkspaceProjectWaitingForModel waitingForModel = projectWaitingForNode.waitingForModel();
                  TasksModel tasksModel = model.getRoot().getUserObject().tasks();
                  view = obf.newObjectBuilder( WorkspaceProjectWaitingForView.class ).use( waitingForModel,
                        projectWaitingForNode.getParent(),
                        tasksModel ).newInstance();

                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        waitingForModel.refresh();

                        return null;
                     }
                  } );
               }

               pane.setRightComponent( view );
            } else
            {
               pane.setRightComponent( new JPanel() );
            }
         }
      } );

      workspaceTree.addTreeWillExpandListener( new TreeWillExpandListener()
      {
         public void treeWillExpand( TreeExpansionEvent event ) throws ExpandVetoException
         {
            Object node = event.getPath().getLastPathComponent();
            if (node instanceof WorkspaceProjectsNode)
            {
               try
               {
                  ((WorkspaceProjectsNode) node).refresh();
                  model.reload( (TreeNode) node );
               } catch (Exception e)
               {
                  e.printStackTrace();
               }
            }
         }

         public void treeWillCollapse( TreeExpansionEvent event ) throws ExpandVetoException
         {
         }
      } );
   }

   public String getSelectedUser()
   {
      return model.getRoot().getUserObject().settings().userName().get();
   }

   public JSplitPane getPane()
   {
      return pane;
   }

   public void setModel( WorkspaceModel model )
   {
      this.model = model;
      workspaceTree.setModel( model );
      accountName = model.getRoot().getUserObject().settings().name().get();
      refreshTree();
   }

   public void refreshTree()
   {
      try
      {
         model.getRoot().getProjectsNode().refresh();
         model.reload( model.getRoot().getProjectsNode() );
         workspaceTree.clearSelection();
         workspaceTree.expandAll();
      } catch (Exception e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh_projects, e );
      }
   }

   @Action
   public void selectTree()
   {
      workspaceTree.requestFocusInWindow();
   }

   @Action
   public void selectTable()
   {
      Component right = pane.getRightComponent();
      if (right != null)
      {
         if (right instanceof TaskTableView)
         {
            TaskTableView ttv = (TaskTableView) right;
            ttv.getTaskTable().requestFocusInWindow();
         } else
            right.requestFocusInWindow();

      }
   }

   @Action
   public void selectDetails()
   {
      Component right = pane.getRightComponent();
      if (right != null)
      {
         if (right instanceof TaskTableView)
         {
            TaskTableView ttv = (TaskTableView) right;
            ttv.getTaskDetails().requestFocusInWindow();
         } else
            right.requestFocusInWindow();
      }
   }
}
