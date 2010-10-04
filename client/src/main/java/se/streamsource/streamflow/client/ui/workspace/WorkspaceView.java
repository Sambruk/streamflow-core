/**
 *
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.workspace;

import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.WrappingIconPanel;
import org.jdesktop.swingx.renderer.WrappingProvider;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.client.ui.caze.AssignmentsCaseTableFormatter;
import se.streamsource.streamflow.client.ui.caze.CaseCreationNode;
import se.streamsource.streamflow.client.ui.caze.CaseDetailView;
import se.streamsource.streamflow.client.ui.caze.CaseTableView;
import se.streamsource.streamflow.client.ui.caze.CasesDetailView2;
import se.streamsource.streamflow.client.ui.caze.CasesModel;
import se.streamsource.streamflow.client.ui.caze.CasesTableModel;
import se.streamsource.streamflow.client.ui.caze.CasesView;
import se.streamsource.streamflow.client.ui.caze.InboxCaseTableFormatter;
import se.streamsource.streamflow.client.ui.search.SearchResultTableModel;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;

/**
 * JAVADOC
 */
public class WorkspaceView
      extends JPanel
{
   @Service
   DialogService dialogs;

   @Structure
   ObjectBuilderFactory obf;

   SearchResultTableModel searchResultTableModel;

   private JXTree workspaceTree;
   private JScrollPane workspaceTreeScroll;
   private WorkspaceModel model;
   private String accountName = "";
   private JLabel selectedContext;
   private JButton selectContextButton;
   private JTextField searchField;

   private Component currentSelection = new JLabel( text( WorkspaceResources.welcome ), JLabel.CENTER );
   private Popup popup;

   private SearchView searchView;


   private JPanel topPanel;
   private CardLayout topLayout = new CardLayout();

   private CasesDetailView2 detailView;
   private CasesModel casesModel;

 

   public WorkspaceView( final @Service ApplicationContext context,
                         final @Structure ObjectBuilderFactory obf )
   {
      setLayout( new BorderLayout() );
      this.setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );

      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), "selectTree" );
      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), "selectTable" );
      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), "selectDetails" );
      setActionMap( context.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( context.getActionMap(
            WorkspaceView.class, this ) );

      final ActionMap am = getActionMap();

      // Create Case
      javax.swing.Action createCaseAction = am.get( "createCase" );
      createCaseAction.setEnabled( false );
      JButton createCaseButton = new JButton( createCaseAction );
//      NotificationGlassPane.registerButton(createCaseButton);
      createCaseButton.registerKeyboardAction( createCaseAction, (KeyStroke) createCaseAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      // Refresh case list
      javax.swing.Action refreshAction = am.get( "refresh" );
      JButton refreshButton = new JButton( refreshAction );
//      NotificationGlassPane.registerButton(refreshButton);
      refreshButton.registerKeyboardAction( refreshAction, (KeyStroke) refreshAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      // Show search
      javax.swing.Action showSearchAction = am.get( "showSearch" );
      JButton showSearchButton = new JButton( showSearchAction );
//      NotificationGlassPane.registerButton(showSearchButton);
      showSearchButton.registerKeyboardAction( showSearchAction, (KeyStroke) showSearchAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      MacOsUIWrapper.convertAccelerators( getActionMap() );

      JPanel contextToolbar = new JPanel();
      contextToolbar.add( createCaseButton );
      contextToolbar.add( refreshButton );
      contextToolbar.add( showSearchButton );

      JPanel contextPanel = new JPanel( new BorderLayout() );
      JPanel leftContext = new JPanel( new FlowLayout(FlowLayout.LEFT, 0, 0));
      leftContext.setBorder( BorderFactory.createEmptyBorder( 5,0,0,0 ));
      selectContextButton = new JButton( getActionMap().get( "selectContext" ) );
      leftContext.add( selectContextButton );
      selectedContext = new JLabel();
      selectedContext.setFont( selectedContext.getFont().deriveFont( Font.BOLD ) );
   

      leftContext.add( selectedContext );
      contextPanel.add( leftContext, BorderLayout.WEST );
      contextPanel.add( contextToolbar, BorderLayout.EAST );

      JPanel searchPanel = new JPanel( new BorderLayout() );
      JPanel searchButtons = new JPanel();
      searchButtons.add( new JButton( getActionMap().get( "hideSearch" ) ) );
      searchPanel.add( searchButtons, BorderLayout.EAST );

      searchView = obf.newObjectBuilder( SearchView.class ).use().newInstance();
      searchField = searchView.getTextField();
      searchField.setAction( getActionMap().get( "search" ) );
      searchPanel.add( searchView, BorderLayout.CENTER );

      topPanel = new JPanel( topLayout );
      topPanel.setBorder( BorderFactory.createEmptyBorder( 0, 0 , 5, 0 ) );
      topPanel.add( contextPanel, "context" );
      topPanel.add( searchPanel, "search" );

      add( topPanel, BorderLayout.NORTH );
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
                     return i18n.icon( Icons.user, i18n.ICON_16 );
                  else if (o instanceof WorkspaceProjectNode)
                     return i18n.icon( Icons.project, i18n.ICON_16 );
                  else if (o instanceof WorkspaceProjectsNode)
                     return i18n.icon( Icons.projects, i18n.ICON_16 );
                  else if (o instanceof WorkspaceUserDraftsNode || o instanceof WorkspaceProjectInboxNode)
                     return i18n.icon( Icons.inbox, i18n.ICON_16 );
                  else if (o instanceof WorkspaceProjectAssignmentsNode)
                     return i18n.icon( Icons.assign, i18n.ICON_16 );
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
                     return text( WorkspaceResources.projects_node );
                  else if (o instanceof WorkspaceUserDraftsNode || o instanceof WorkspaceProjectInboxNode)
                     return o.toString();
                  else if (o instanceof WorkspaceProjectAssignmentsNode)
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

               if (node instanceof WorkspaceUserDraftsNode)
               {
                  WorkspaceUserDraftsNode userDraftsNode = (WorkspaceUserDraftsNode) node;
                  final CasesTableModel inboxModel = userDraftsNode.caseTableModel();
                  view = obf.newObjectBuilder( CaseTableView.class ).use( inboxModel, detailView,
                        userDraftsNode.getParent(), node,
                        casesModel, new InboxCaseTableFormatter()
                  ).newInstance();

                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        inboxModel.refresh();

                        return null;
                     }
                  } );
               } else if (node instanceof WorkspaceProjectInboxNode)
               {
                  WorkspaceProjectInboxNode projectInboxNode = (WorkspaceProjectInboxNode) node;
                  final CasesTableModel inboxModel = projectInboxNode.caseTableModel();
                  view = obf.newObjectBuilder( CaseTableView.class ).use( inboxModel, detailView,
                        projectInboxNode.getParent(),
                        casesModel,
                        new InboxCaseTableFormatter() ).newInstance();

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
                  final CasesTableModel assignmentsModel = projectAssignmentsNode.caseTableModel();
                  view = obf.newObjectBuilder( CaseTableView.class ).use( assignmentsModel, detailView,
                        node,
                        projectAssignmentsNode.getParent(),
                        casesModel,
                        new AssignmentsCaseTableFormatter() ).newInstance();

                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        assignmentsModel.refresh();

                        return null;
                     }
                  } );

               }

               // Determine whether the Create case button should be enabled
               am.get( "createCase" ).setEnabled( node instanceof CaseCreationNode );

               if (view != null)
               {
                  CasesView casesView = obf.newObjectBuilder( CasesView.class ).use( view, detailView ).newInstance();

                  remove( currentSelection );
                  currentSelection = casesView;
                  add( currentSelection, BorderLayout.CENTER );

                  String nodeString = node.toString().split( "\\(" )[0];
                  String selectedContextText = ((TreeNode) node).getParent() + " : " + nodeString;

                  selectedContext.setOpaque( true );
                  UIDefaults uiDefaults = UIManager.getDefaults();
                  selectedContext.setBackground( uiDefaults.getColor( "Menu.selectionBackground" ) );
                  selectedContext.setForeground( uiDefaults.getColor( "Menu.selectionForeground" ) );
                  selectedContext.setText( "  " + selectedContextText + " " );
                  FontMetrics fm = selectedContext.getFontMetrics( selectedContext.getFont() );
                  int width = fm.stringWidth( selectedContext.getText() );
                  selectedContext.setPreferredSize( new Dimension( width , 22 ));

                  // show blank detail view
                  detailView.show( null );

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
               selectedContext.setOpaque( false );
               selectedContext.setBackground( selectedContext.getParent().getBackground() );
               selectedContext.setForeground( selectedContext.getParent().getForeground() );
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

      workspaceTreeScroll = new JScrollPane( workspaceTree );

      //TODO SF-278 - this solution does not work sufficient - we have to come up with something else
      /*workspaceTreeScroll.addMouseListener( new MouseAdapter()
      {

         @Override
         public void mouseExited( MouseEvent e )
         {
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
      } );*/
   }

   public void setModel( AccountModel model )
   {
      this.model = model.workspace();
      workspaceTree.clearSelection();
      workspaceTree.setModel( model.workspace() );
      accountName = model.workspace().getRoot().getUserObject().settings().name().get();

      searchResultTableModel = model.search();

      casesModel = model.workspace().getRoot().getUserObject().cases();
      detailView = obf.newObjectBuilder( CasesDetailView2.class ).use( casesModel ).newInstance();
      searchView.setModel( this.model.getSavedSearches() );

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
   public Task selectContext()
   {
      if (popup == null)
      {
         Point location = selectContextButton.getLocationOnScreen();
         popup = PopupFactory.getSharedInstance().getPopup( this, workspaceTreeScroll, (int) location.getX(), (int) location.getY() + selectContextButton.getHeight() );
         popup.show();

         return obf.newObjectBuilder( RefreshCaseCountTask.class ).use( workspaceTree, model.getRoot() ).newInstance();
      } else
      {
         popup.hide();
         popup = null;
         return null;
      }
   }

   @Action
   public void showSearch()
   {
      topLayout.show( topPanel, "search" );
      searchField.requestFocusInWindow();

      CaseTableView view = obf.newObjectBuilder( CaseTableView.class ).
            use( searchResultTableModel,
                  detailView,
                  casesModel,
                  new InboxCaseTableFormatter() ).newInstance();

      CasesView casesView = obf.newObjectBuilder( CasesView.class ).use( view, detailView ).newInstance();

      remove( currentSelection );
      currentSelection = casesView;
      add( currentSelection, BorderLayout.CENTER );


      add( currentSelection, BorderLayout.CENTER );
   }

   @Action
   public void hideSearch()
   {
      topLayout.show( topPanel, "context" );

      TreePath[] selection = workspaceTree.getSelectionPaths();
      workspaceTree.clearSelection();
      workspaceTree.setSelectionPaths( selection );
      // request focus to enable accelerator keys for workspace buttons again
      this.requestFocus();
   }

   @Action
   public void search()
   {
      String searchString = searchField.getText();

      if (searchString.length() > 500)
      {
         dialogs.showMessageDialog( this, text( WorkspaceResources.too_long_query), "" );
      } else
      {
         searchResultTableModel.search( searchString );
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
      if (currentSelection instanceof CaseTableView)
      {
         CaseTableView ttv = (CaseTableView) currentSelection;
         ttv.getCaseTable().requestFocusInWindow();
      } else
         currentSelection.requestFocusInWindow();
   }

   @Action
   public void selectDetails()
   {
      if (currentSelection instanceof CaseTableView)
      {
         CaseTableView ttv = (CaseTableView) currentSelection;
         ttv.getCaseDetails().requestFocusInWindow();
      } else
         currentSelection.requestFocusInWindow();
   }

   @Action
   public void createCase()
   {
      TreePath path = workspaceTree.getSelectionPath();
      if (path != null)
      {
         Object node = path.getLastPathComponent();

         if (node instanceof CaseCreationNode)
         {
            // TODO very odd hack - how to solve state binder update issue during use of accelerator keys.
            Component focusOwner = WindowUtils.findWindow( this ).getFocusOwner();
            focusOwner.transferFocus();

            ((CaseCreationNode) node).createDraft();
            refresh();
            CasesView currentCases = (CasesView) currentSelection;
            JXTable caseTable = currentCases.getCaseTableView().getCaseTable();
            caseTable.getSelectionModel().setSelectionInterval( caseTable.getRowCount() - 1, caseTable.getRowCount() - 1 );
            caseTable.scrollRowToVisible( caseTable.getSelectedRow() );

            final CaseDetailView caseDetail = currentCases.getCurrentCaseView();
            SwingUtilities.invokeLater( new Runnable()
            {
               public void run()
               {
                  caseDetail.setSelectedTab( 0 );
                  caseDetail.requestFocusInWindow();
               }
            } );
         }
      }

   }

   @Action
   public void refresh()
   {
      if (currentSelection instanceof CasesView)
      {
         CasesView currentCases = (CasesView) currentSelection;
         currentCases.refresh();
      }
   }
}
