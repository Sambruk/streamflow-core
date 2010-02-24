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
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.client.ui.search.SearchResultTableModel;
import se.streamsource.streamflow.client.ui.task.AssignmentsTaskTableFormatter;
import se.streamsource.streamflow.client.ui.task.DelegationsTaskTableFormatter;
import se.streamsource.streamflow.client.ui.task.InboxTaskTableFormatter;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.client.ui.task.TaskTableView;
import se.streamsource.streamflow.client.ui.task.TasksDetailView2;
import se.streamsource.streamflow.client.ui.task.TasksModel;
import se.streamsource.streamflow.client.ui.task.TasksView;
import se.streamsource.streamflow.client.ui.task.WaitingForTaskTableFormatter;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * JAVADOC
 */
public class WorkspaceView
      extends JPanel
{
   @Structure
   ObjectBuilderFactory obf;

   SearchResultTableModel searchResultTableModel;

   private JXTree workspaceTree;
   private WorkspaceModel model;
   private String accountName = "";
   private JLabel selectedContext;
   private JButton selectContextButton;
   private JTextField searchField;

   private Component currentSelection = new JLabel( "<html><h1>Welcome to StreamFlow</h1>Begin by selecting a context by using the blue button</html>", JLabel.CENTER );
   public Popup popup;
   public JPanel contextPanel;
   public TasksDetailView2 detailView;
   public TasksModel tasksModel;

   public WorkspaceView( final @Service ApplicationContext context,
                         final @Structure ObjectBuilderFactory obf )
   {
      setLayout( new BorderLayout() );

      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), "selectTree" );
      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), "selectTable" );
      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), "selectDetails" );
      setActionMap( context.getActionMap( this ) );


      contextPanel = new JPanel( new BorderLayout() );
      selectContextButton = new JButton( getActionMap().get( "selectContext" ) );
      contextPanel.add( selectContextButton, BorderLayout.WEST );
      selectedContext = new JLabel();
      selectedContext.setFont( selectedContext.getFont().deriveFont( Font.ITALIC ) );
      contextPanel.add( selectedContext, BorderLayout.CENTER );
      contextPanel.add( new JButton( getActionMap().get( "showSearch" ) ), BorderLayout.EAST );
      searchField = new JTextField();
      searchField.setAction( getActionMap().get( "search" ) );

      add( contextPanel, BorderLayout.NORTH );
      add( currentSelection, BorderLayout.CENTER );

      workspaceTree = new JXTree();
      workspaceTree.expandAll();
      workspaceTree.setRootVisible( false );
      workspaceTree.setShowsRootHandles( false );
      workspaceTree.getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

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

      workspaceTree.addTreeSelectionListener( new TreeSelectionListener()
      {
         public void valueChanged( TreeSelectionEvent e )
         {
            final TreePath path = e.getNewLeadSelectionPath();
            if (path != null)
            {
               Object node = path.getLastPathComponent();

               JComponent view = null;

               if (node instanceof WorkspaceUserInboxNode)
               {
                  WorkspaceUserInboxNode userInboxNode = (WorkspaceUserInboxNode) node;
                  final TaskTableModel inboxModel = userInboxNode.taskTableModel();
                  view = obf.newObjectBuilder( TaskTableView.class ).use( inboxModel, detailView,
                        userInboxNode.getParent(), node,
                        tasksModel, new InboxTaskTableFormatter()
                  ).newInstance();

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
                  final TaskTableModel assignmentsModel = userAssignmentsNode.taskTableModel();
                  view = obf.newObjectBuilder( TaskTableView.class ).use( assignmentsModel, detailView,
                        userAssignmentsNode.getParent(), node,
                        tasksModel, new AssignmentsTaskTableFormatter()
                  ).newInstance();

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
                  final TaskTableModel delegationsModel = userDelegationsNode.taskTableModel();
                  view = obf.newObjectBuilder( TaskTableView.class ).use( delegationsModel, detailView,
                        userDelegationsNode.getParent(),
                        tasksModel,
                        new DelegationsTaskTableFormatter() ).newInstance();

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
                  final TaskTableModel waitingForModel = userWaitingForNode.taskTableModel();
                  view = obf.newObjectBuilder( TaskTableView.class ).use( waitingForModel, detailView,
                        userWaitingForNode.getParent(),
                        tasksModel,
                        new WaitingForTaskTableFormatter() ).newInstance();

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
                  final TaskTableModel inboxModel = projectInboxNode.taskTableModel();
                  view = obf.newObjectBuilder( TaskTableView.class ).use( inboxModel, detailView,
                        projectInboxNode.getParent(),
                        tasksModel,
                        new InboxTaskTableFormatter() ).newInstance();

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
                  final TaskTableModel assignmentsModel = projectAssignmentsNode.taskTableModel();
                  view = obf.newObjectBuilder( TaskTableView.class ).use( assignmentsModel, detailView,
                        node,
                        projectAssignmentsNode.getParent(),
                        tasksModel,
                        new AssignmentsTaskTableFormatter() ).newInstance();

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
                  final TaskTableModel delegationsModel = projectDelegationsNode.taskTableModel();
                  view = obf.newObjectBuilder( TaskTableView.class ).use( delegationsModel, detailView,
                        projectDelegationsNode.getParent(),
                        tasksModel,
                        new DelegationsTaskTableFormatter() ).newInstance();

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
                  final TaskTableModel waitingForModel = projectWaitingForNode.taskTableModel();
                  view = obf.newObjectBuilder( TaskTableView.class ).use( waitingForModel, detailView,
                        projectWaitingForNode.getParent(),
                        tasksModel,
                        new WaitingForTaskTableFormatter() ).newInstance();

                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        waitingForModel.refresh();

                        return null;
                     }
                  } );
               }

               if (view != null)
               {
                  TasksView tasksView = obf.newObjectBuilder( TasksView.class ).use( view, detailView ).newInstance();

                  remove( currentSelection );
                  currentSelection = tasksView;
                  add( currentSelection, BorderLayout.CENTER );

                  String selectedContextText = ((TreeNode) node).getParent() + " : " + node.toString();

                  selectedContext.setText( selectedContextText );

                  SwingUtilities.invokeLater( new Runnable()
                  {
                     public void run()
                     {
                        if (popup != null)
                        {
                           popup.hide();
                           popup = null;
                        }
                     }
                  } );
               }


            } else
            {
               selectedContext.setText( "" );
               remove( currentSelection );
               currentSelection = new JLabel();
               add( currentSelection, BorderLayout.CENTER );
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

   public void setModel( AccountModel model )
   {
      this.model = model.workspace();
      workspaceTree.clearSelection();
      workspaceTree.setModel( model.workspace() );
      accountName = model.workspace().getRoot().getUserObject().settings().name().get();

      searchResultTableModel = model.search();

      tasksModel = model.workspace().getRoot().getUserObject().tasks();
      detailView = obf.newObjectBuilder( TasksDetailView2.class ).use( tasksModel ).newInstance();

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
   public void selectContext()
   {
      if (popup == null)
      {
         contextPanel.remove( searchField );
         contextPanel.add( selectedContext, BorderLayout.CENTER );

         Point location = selectContextButton.getLocationOnScreen();
         popup = PopupFactory.getSharedInstance().getPopup( this, workspaceTree, (int) location.getX(), (int) location.getY() + selectContextButton.getHeight() );
         popup.show();
      } else
      {
         popup.hide();
         popup = null;
      }
   }

   @Action
   public void showSearch()
   {
      contextPanel.remove( selectedContext );
      contextPanel.add( searchField, BorderLayout.CENTER );
      contextPanel.revalidate();
      contextPanel.repaint();
      searchField.requestFocusInWindow();

      TaskTableView view = obf.newObjectBuilder( TaskTableView.class ).
            use( searchResultTableModel,
                  detailView,
                  tasksModel,
                  new InboxTaskTableFormatter() ).newInstance();

      TasksView tasksView = obf.newObjectBuilder( TasksView.class ).use( view, detailView ).newInstance();

      remove( currentSelection );
      currentSelection = tasksView;
      add( currentSelection, BorderLayout.CENTER );


      add( currentSelection, BorderLayout.CENTER );
   }

   @Action
   public void search()
   {
      searchResultTableModel.search( searchField.getText() );
   }

   @Action
   public void selectTree()
   {
      workspaceTree.requestFocusInWindow();
   }

   @Action
   public void selectTable()
   {
      if (currentSelection instanceof TaskTableView)
      {
         TaskTableView ttv = (TaskTableView) currentSelection;
         ttv.getTaskTable().requestFocusInWindow();
      } else
         currentSelection.requestFocusInWindow();
   }

   @Action
   public void selectDetails()
   {
      if (currentSelection instanceof TaskTableView)
      {
         TaskTableView ttv = (TaskTableView) currentSelection;
         ttv.getTaskDetails().requestFocusInWindow();
      } else
         currentSelection.requestFocusInWindow();
   }
}
