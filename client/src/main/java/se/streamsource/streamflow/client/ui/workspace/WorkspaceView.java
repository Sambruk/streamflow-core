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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
import se.streamsource.streamflow.client.ui.workspace.context.WorkspaceContextView;
import se.streamsource.streamflow.client.ui.workspace.search.SearchResultTableModel;
import se.streamsource.streamflow.client.ui.workspace.search.SearchView;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableFormatter;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableView;
import se.streamsource.streamflow.client.ui.workspace.table.CasesView;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RoundedBorder;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.forms.factories.Borders;

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

   private WorkspaceContextView contextView;
   private JLabel selectedContext;
   private JButton selectContextButton;
   private JButton createCaseButton;
   private JButton filterButton;

   private JDialog popup;

   private JPanel contextToolbar;
   private JPanel contextPanel;
   private CardLayout topLayout = new CardLayout();

   private CasesView casesView;
   private final ObjectBuilderFactory obf;
   private final CommandQueryClient client;

   private SearchView searchView;


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

      casesView = obf.newObjectBuilder( CasesView.class ).use(client).newInstance();

      // Create Case
      javax.swing.Action createCaseAction = am.get( "createCase" );
      createCaseButton = new JButton( createCaseAction );
      createCaseButton.registerKeyboardAction( createCaseAction, (KeyStroke) createCaseAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      // Refresh case list
      javax.swing.Action refreshAction = am.get( "refresh" );
      JButton refreshButton = new JButton( refreshAction );
      refreshButton.registerKeyboardAction( refreshAction, (KeyStroke) refreshAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      // Filter search
      javax.swing.Action filterAction = am.get( "filter" );
      filterButton = new JButton( filterAction );
      filterButton.registerKeyboardAction( filterAction, (KeyStroke) filterAction
            .getValue( javax.swing.Action.ACCELERATOR_KEY ),
            JComponent.WHEN_IN_FOCUSED_WINDOW );

      MacOsUIWrapper.convertAccelerators( getActionMap() );

      JPanel topPanel = new JPanel( new BorderLayout());
      selectContextButton = new JButton( getActionMap().get( "selectContext" ) );
      JPanel contextSelectionPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
      contextSelectionPanel.setBorder( BorderFactory.createEmptyBorder( 5, 0, 0, 0 ) );
      contextSelectionPanel.add( selectContextButton );
      selectedContext = new JLabel();
      selectedContext.setFont( selectedContext.getFont().deriveFont( Font.BOLD ) );
      contextSelectionPanel.add( selectedContext );
      topPanel.add(contextSelectionPanel, BorderLayout.WEST);
      
      contextToolbar = new JPanel();
      contextToolbar.add( filterButton );
      contextToolbar.add( createCaseButton );
      contextToolbar.add( refreshButton);
      topPanel.add(contextToolbar, BorderLayout.EAST);
      contextToolbar.setVisible( false );
      
      searchResultTableModel = obf.newObjectBuilder( SearchResultTableModel.class ).use( client.getSubClient("search") ).newInstance();

      searchView = obf.newObjectBuilder( SearchView.class ).use(searchResultTableModel, client).newInstance();
      topPanel.add( searchView, BorderLayout.CENTER );
      searchView.setVisible(false);

      add( topPanel, BorderLayout.NORTH );
      add( casesView, BorderLayout.CENTER );

      contextView = obf.newObjectBuilder( WorkspaceContextView.class ).use( client ).newInstance();
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
                  boolean isSearch = contextItem.getRelation().equals("search");
                  searchView.setVisible(isSearch);
                  contextToolbar.setVisible( true );
                  
                  TableFormat tableFormat;
                  CasesTableView casesTable;
                  tableFormat = new CasesTableFormatter();
                  casesTable = obf.newObjectBuilder(CasesTableView.class).use(isSearch ? searchResultTableModel : contextItem.getClient(), tableFormat)
                        .newInstance();

                  casesTable.getCaseTable().getSelectionModel().addListSelectionListener(new CaseSelectionListener());

                  casesView.showTable(casesTable);

                  setContextString(contextItem);

                  createCaseButton.setVisible(contextItem.getRelation().equals("assign") || contextItem.getRelation().equals("draft"));
                  filterButton.setVisible(isSearch);

               } else
               {
                  setContextString(contextItem);
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
         int width = fm.stringWidth( selectedContext.getText() )+15;
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
         final JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass( JFrame.class, this );
         popup = new JDialog( frame );
         popup.setUndecorated( true );
         popup.setModal( false );
         popup.setLayout( new BorderLayout() );
         popup.add( contextView, BorderLayout.CENTER );
         Point location = selectContextButton.getLocationOnScreen();
         popup.setBounds( (int) location.getX(), (int) location.getY() + selectContextButton.getHeight(), contextView.getWidth(), contextView.getHeight() );
         popup.pack();
         popup.setVisible( true );
         frame.addComponentListener( new ComponentAdapter()
         {
            @Override
            public void componentMoved( ComponentEvent e )
            {
               if(popup != null )
               {
                  killPopup();
                  frame.removeComponentListener( this );
               }
            }
         } );
      } else
      {
         killPopup();
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

   @Action
   public void filter()
   {
      casesView.toogleFilterVisible();
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
                     Object valueAt = caseTable.getModel().getValueAt( caseTable.convertRowIndexToModel( selectedRow ), 8 );
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
