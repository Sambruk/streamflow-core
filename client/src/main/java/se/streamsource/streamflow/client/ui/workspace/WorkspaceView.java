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

import ca.odell.glazedlists.gui.TableFormat;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.ui.workspace.context.WorkspaceContextView2;
import se.streamsource.streamflow.client.ui.workspace.search.SearchResultTableModel;
import se.streamsource.streamflow.client.ui.workspace.search.SearchView;
import se.streamsource.streamflow.client.ui.workspace.table.*;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RoundedBorder;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * JAVADOC
 */
public class WorkspaceView
      extends JPanel
      implements TransactionListener
{
   @Service
   DialogService dialogs;

   SearchResultTableModel searchResultTableModel;

   private WorkspaceContextView2 contextView;
   private JLabel selectedContext;
   private JButton selectContextButton;
   private JTextField searchField;

   private JDialog popup;

   private JPanel topPanel;
   private CardLayout topLayout = new CardLayout();

   private CasesView casesView;
   private final ObjectBuilderFactory obf;
   private final CommandQueryClient client;


   public WorkspaceView( final @Service ApplicationContext context,
                         final @Structure ObjectBuilderFactory obf,
                         final @Uses CommandQueryClient client )
   {
      this.obf = obf;
      this.client = client;
      setLayout( new BorderLayout() );
      this.setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );

      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), "selectTree" );
      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), "selectTable" );
      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ), "selectDetails" );
      setActionMap( context.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( context.getActionMap(
            WorkspaceView.class, this ) );

      final ActionMap am = getActionMap();

      casesView = obf.newObject( CasesView.class );

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
      JPanel leftContext = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
      leftContext.setBorder( BorderFactory.createEmptyBorder( 5, 0, 0, 0 ) );
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

      searchResultTableModel = obf.newObjectBuilder( SearchResultTableModel.class ).use( client ).newInstance();

      SearchView searchView = obf.newObjectBuilder( SearchView.class ).use(client.getSubClient( "savedsearches" )).newInstance();
      searchField = searchView.getTextField();
      searchField.setAction( getActionMap().get( "search" ) );
      searchPanel.add( searchView, BorderLayout.CENTER );

      topPanel = new JPanel( topLayout );
      topPanel.add( contextPanel, "context" );
      topPanel.add( searchPanel, "search" );

      add( topPanel, BorderLayout.NORTH );
      add( casesView, BorderLayout.CENTER );

      contextView = obf.newObjectBuilder( WorkspaceContextView2.class ).use( client.getSubClient( "context" )).newInstance();
      JList workspaceContextList = contextView.getWorkspaceContextList();
      workspaceContextList.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               JList list = (JList) e.getSource();

               try
               {
                  if (list.getSelectedValue() == null)
                  {
                     casesView.clearCase();
                     return;
                  }

                  if (!(list.getSelectedValue() instanceof ContextItem))
                     return;
               } catch (IndexOutOfBoundsException e1)
               {
                  return; // Can get this if filtering with selection
               }

               ContextItem contextItem = (ContextItem) list.getSelectedValue();
               if (contextItem != null)
               {
                  TableFormat tableFormat;
                  CasesTableView casesTable;
                  tableFormat = new CasesTableFormatter();
                  casesTable = obf.newObjectBuilder( CasesTableView.class ).use( contextItem.getClient(), tableFormat ).newInstance();

                  casesTable.getCaseTable().getSelectionModel().addListSelectionListener( new CaseSelectionListener() );

                  casesView.showTable( casesTable );

                  setContextString( contextItem );

                  am.get( "createCase" ).setEnabled( !(contextItem.getGroup() != null && contextItem.getRelation().equals( Icons.inbox.name() )) );
               } else
               {
                  setContextString( contextItem );
               }

               killPopup();
            }
         }
      } );
   }

   private void setContextString( ContextItem contextItem )
   {
      if (contextItem != null)
      {
         selectedContext.setOpaque( true );
         UIDefaults uiDefaults = UIManager.getDefaults();
         selectedContext.setBackground( uiDefaults.getColor( "Menu.selectionBackground" ) );
         selectedContext.setForeground( uiDefaults.getColor( "Menu.selectionForeground" ) );
         selectedContext.setBorder( new RoundedBorder( ) );

         String text = "";
         if (contextItem.getGroup().equals( "" ))
         {
            text += contextItem.getName();
         } else
         {
            text += contextItem.getGroup() + " : " + contextItem.getName();
         }

         selectedContext.setText( "  " + text + " " );
         FontMetrics fm = selectedContext.getFontMetrics( selectedContext.getFont() );
         int width = fm.stringWidth( selectedContext.getText() )+selectedContext.getHeight()*2;
         selectedContext.setPreferredSize( new Dimension( width, 22 ) );
      } else
      {
         selectedContext.setOpaque( false );
         selectedContext.setBackground( selectedContext.getParent().getBackground() );
         selectedContext.setForeground( selectedContext.getParent().getForeground() );
         selectedContext.setText( "" );
      }
   }


   @Action
   public void selectContext()
   {
      if (popup == null)
      {
         JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass( JFrame.class, this );
         popup = new JDialog( frame );
         popup.setUndecorated( true );
         popup.setModal( false );
         popup.setLayout( new BorderLayout() );
         popup.add( contextView, BorderLayout.CENTER );
         Point location = selectContextButton.getLocationOnScreen();
         popup.setBounds( (int) location.getX(), (int) location.getY() + selectContextButton.getHeight(), contextView.getWidth(), contextView.getHeight() );
         popup.pack();
         popup.setVisible( true );
      } else
      {
         killPopup();
      }
   }

   @Action
   public void showSearch()
   {
      topLayout.show( topPanel, "search" );
      searchField.requestFocusInWindow();

      CasesTableView casesTable = obf.newObjectBuilder( CasesTableView.class ).
            use( searchResultTableModel, new CasesTableFormatter()).newInstance();
      casesTable.getCaseTable().getSelectionModel().addListSelectionListener( new CaseSelectionListener() );

      casesView.showTable( casesTable );
   }

   @Action
   public void hideSearch()
   {
      topLayout.show( topPanel, "context" );

      int selectedContext = contextView.getWorkspaceContextList().getSelectedIndex();
      if (selectedContext != -1)
      {
         contextView.getWorkspaceContextList().clearSelection();
         contextView.getWorkspaceContextList().setSelectedIndex( selectedContext );
      }

      // request focus to enable accelerator keys for workspace buttons again
//      this.requestFocus();
   }

   @Action
   public void search()
   {

      final String searchString = searchField.getText();

      if (searchString.length() > 500)
      {
         dialogs.showMessageDialog( this, i18n.text( WorkspaceResources.too_long_query), "" );
      } else
      {
         searchResultTableModel.search( searchString );
      }
   }

   @Action
   public Task createCase()
   {
      final ContextItem contextItem = (ContextItem) contextView.getWorkspaceContextList().getSelectedValue();

      if (contextItem != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               contextItem.getClient().postCommand( "createcase" );
            }
         };
      } else
         return null;
   }

   @Action
   public void refresh()
   {
      casesView.refresh();
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
   }

   public void killPopup()
   {
      if( popup != null )
      {
         popup.setVisible(false);
         popup.dispose();
         popup = null;
      }
   }

   class CaseSelectionListener
         implements ListSelectionListener
   {
      public void valueChanged( ListSelectionEvent e )
      {
         if (!e.getValueIsAdjusting())
         {
            JTable caseTable = casesView.getCaseTableView().getCaseTable();

            try
            {
               if (!caseTable.getSelectionModel().isSelectionEmpty())
               {
                  int selectedRow = caseTable.getSelectedRow();
                  if (selectedRow != -1)
                  {
                     Object valueAt = caseTable.getModel().getValueAt( caseTable.convertRowIndexToModel( selectedRow ), 5 );
                     if (valueAt instanceof String)
                     {
                        String href = (String) valueAt;
                        casesView.showCase( client.getClient( href ));
                     }
                  }
               }
            } catch (Exception e1)
            {
               throw new OperationException( CaseResources.could_not_view_details, e1 );
            }
         }
      }
   }

}
