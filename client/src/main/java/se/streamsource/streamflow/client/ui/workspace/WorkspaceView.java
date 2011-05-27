/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import ca.odell.glazedlists.gui.*;
import com.jgoodies.forms.factories.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.ui.*;
import se.streamsource.streamflow.client.ui.workspace.context.*;
import se.streamsource.streamflow.client.ui.workspace.search.*;
import se.streamsource.streamflow.client.ui.workspace.table.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.resource.user.profile.*;
import se.streamsource.streamflow.util.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * JAVADOC
 */
public class WorkspaceView
      extends JPanel
      implements TransactionListener
{
   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   SearchResultTableModel searchResultTableModel;

   private WorkspaceContextView contextView;
   private JLabel selectedContext;
   private JButton selectContextButton;
   private JButton createCaseButton;

   private JDialog popup;

   private JPanel contextToolbar;
   private JPanel contextPanel;
   private CardLayout topLayout = new CardLayout();

   private CasesView casesView;
   private final ObjectBuilderFactory obf;
   private final CommandQueryClient client;

   private SearchView searchView;
   private javax.swing.Action managePerspectives;
   private javax.swing.Action savePerspective;

   private CasesTableView casesTable;


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

      // Proxy menu item actions manually
      ApplicationAction managePerspectivesAction = (ApplicationAction) getActionMap().get( "managePerspectives" );
      managePerspectives = context.getActionMap().get( "managePerspectives" );
      managePerspectives.putValue( "proxy", managePerspectivesAction );

      // Proxy menu item actions manually
      ApplicationAction savePerspectiveAction = (ApplicationAction) getActionMap().get( "savePerspective" );
      savePerspective = context.getActionMap().get( "savePerspective" );
      savePerspective.putValue( "proxy", savePerspectiveAction );

      searchResultTableModel = obf.newObjectBuilder( SearchResultTableModel.class ).use( client.getSubClient("search") ).newInstance();

      searchView = obf.newObjectBuilder( SearchView.class ).use(searchResultTableModel, client).newInstance();

      casesView = obf.newObjectBuilder( CasesView.class ).use(client, searchView.getTextField() ).newInstance();

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


      MacOsUIWrapper.convertAccelerators( getActionMap() );

      JPanel topPanel = new JPanel( new BorderLayout());
      selectContextButton = new JButton( getActionMap().get( "selectContext" ) );
      selectContextButton.setName("btnSelectContext");
      JPanel contextSelectionPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
      contextSelectionPanel.setBorder( BorderFactory.createEmptyBorder( 5, 0, 0, 0 ) );
      contextSelectionPanel.add( selectContextButton );
      selectedContext = new JLabel();
      selectedContext.setFont( selectedContext.getFont().deriveFont( Font.BOLD ) );
      contextSelectionPanel.add( selectedContext );
      topPanel.add(contextSelectionPanel, BorderLayout.WEST);
      
      contextToolbar = new JPanel();
      //contextToolbar.add( perspectiveButton );
      contextToolbar.add( createCaseButton );
      contextToolbar.add( refreshButton);
      topPanel.add(contextToolbar, BorderLayout.EAST);
      contextToolbar.setVisible( false );

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
                  boolean isSearch = contextItem.getRelation().equals( "search" );
                  boolean isPerspective = contextItem.getRelation().equals( "perspective" );

                  TableFormat tableFormat;
                  tableFormat = new CasesTableFormatter();
                  
                  if ( isPerspective )
                  {
                     PerspectiveValue perspectiveValue = contextItem.getClient().query( "index", PerspectiveValue.class );
                     String contextRel = perspectiveValue.context().get();

                     for (ContextItem item : contextView.getModel().getItems())
                     {
                        if (contextRel.equals( item.getClient().getReference().toString()) )
                        {
                           contextItem = item;
                           isSearch = contextItem.getRelation().equals( "search" );
                           break;
                        }
                     }
                     casesTable = obf.newObjectBuilder(CasesTableView.class)
                           .use( obf, isSearch ? searchResultTableModel : contextItem.getClient(), tableFormat, isSearch ? searchView.getTextField() : null )
                              .newInstance();

                     casesTable.getModel().setFilter( perspectiveValue );

                     searchView.getTextField().setText(perspectiveValue.query().get());
                     setContextString( contextItem, perspectiveValue.name().get() );

                  } else
                  {
                     casesTable = obf.newObjectBuilder(CasesTableView.class)
                           .use( obf, isSearch ? searchResultTableModel : contextItem.getClient(), tableFormat, isSearch ? searchView.getTextField() : null )
                              .newInstance();

                     searchView.getTextField().setText( "" );
                     searchResultTableModel.clearSearchString();
                     casesTable.getModel().clearFilter();
                     setContextString( contextItem, null );
                  }

                  searchView.setVisible(isSearch);
                  contextToolbar.setVisible( true );

                  casesView.showTable(casesTable);

                  createCaseButton.setVisible( casesTable.getModel().isCreateCaseEnabled() );

               } else
               {
                  setContextString( contextItem, null );
               }

               killPopup();
            }
         }
      } );
      
      addHierarchyListener(new HierarchyListener()
      {
         public void hierarchyChanged(HierarchyEvent e)
         {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED)>0 )
            {
               if( !WorkspaceView.this.isShowing() )
               {
                  killPopup();
                  context.getActionMap().get( "managePerspectives" ).setEnabled( false );
                  context.getActionMap().get( "savePerspective" ).setEnabled( false );
               }
               else
               {
                  context.getActionMap().get( "managePerspectives" ).setEnabled( true );
                  context.getActionMap().get( "savePerspective" ).setEnabled( true );
               }
            }
         }
      });
   }

   private void setContextString( ContextItem contextItem, String perspective )
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

         text = Strings.empty( perspective ) ? text :  text + " : " + perspective;
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
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            casesTable.getModel().clearFilter();
            casesTable.getModel().createCase();
         }
      };
   }

   @Action
   public void refresh()
   {
      casesView.refresh();
   }

   @Action
   public void managePerspectives()
   {
      ManagePerspectivesDialog dialog = obf.newObjectBuilder( ManagePerspectivesDialog.class ).use( client.getSubClient( "perspectives" )).newInstance();
      dialogs.showButtonLessDialog( this, dialog, i18n.text( WorkspaceResources.manage_perspectives ) );
   }

   @Action
   public Task savePerspective()
   {
      final NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( WorkspaceResources.save_perspective ) );
      if (!Strings.empty( dialog.name() ))
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               PerspectivesModel model = obf.newObjectBuilder( PerspectivesModel.class ).use( client.getSubClient( "perspectives" ) ).newInstance();
               PerspectiveValue perspective = casesView.getCaseTableView().getModel().getPerspective( dialog.name(), searchView.isVisible() ? searchView.getTextField().getText() : "" );
               model.savePerspective( perspective );
            }
         };
      } else
         return null;
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

   public WorkspaceContextView getWorkspaceContext()
   {
      return contextView;
   }
}
