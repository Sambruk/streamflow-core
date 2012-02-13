/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.workspace.table;

import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseDetailView;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseResources;
import se.streamsource.streamflow.client.ui.workspace.cases.CasesModel;
import se.streamsource.streamflow.client.util.HtmlPanel;
import se.streamsource.streamflow.client.util.i18n;
import ca.odell.glazedlists.SeparatorList;

/**
 * JAVADOC
 */
public class CasesView
      extends JPanel
{
   private CasesTableView casesTableView;
   private CasesDetailView detailsView;
   
   private JSplitPane splitPane;
   private CardLayout cardLayout = new CardLayout();
   private JComponent blank;
   private CasesModel casesModel;
   private JTextField searchField;
   private JPanel topPanel;

   public CasesView( @Structure Module module, @Service ApplicationContext context, @Uses CasesModel casesModel,
                     @Optional @Uses JTextField searchField)
   {
      super();
      this.casesModel = casesModel;
      this.searchField = searchField;

      setActionMap( context.getActionMap( this ) );

      setLayout( cardLayout );

      this.detailsView = module.objectBuilderFactory().newObjectBuilder(CasesDetailView.class).use(casesModel).newInstance();
      
      
      splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
      splitPane.setOneTouchExpandable( true );

      splitPane.setTopComponent( new JPanel() );
      splitPane.setBottomComponent( detailsView );
      splitPane.setResizeWeight( 0.27D );

      splitPane.setDividerLocation( 1D );
      splitPane.setBorder( BorderFactory.createEmptyBorder() );

      topPanel = new JPanel( new BorderLayout());
      topPanel.add( splitPane, BorderLayout.CENTER);
      
      add( blank = createBlankPanel(), "blank" );
      add( topPanel, "cases" );

      cardLayout.show( this, "blank" );
   }

   protected JPanel createBlankPanel()
   {
      JPanel blankPanel = new JPanel( new BorderLayout() );
      URL logoURL = getClass().getResource( i18n.text( Icons.name_logo ) );
      JEditorPane blankPane = new HtmlPanel(text( WorkspaceResources.welcome, logoURL.toExternalForm() ) );
      blankPanel.add( blankPane, BorderLayout.CENTER );
      return blankPanel;
   }

   public void showTable( final CasesTableView casesTableView )
   {
      cardLayout.show( this, "cases" );
      this.casesTableView = casesTableView;
      this.casesTableView.getCaseTable().getSelectionModel().addListSelectionListener( new CaseSelectionListener() );
      splitPane.setTopComponent( casesTableView );
      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            detailsView.selectCaseInTable( casesTableView.getCaseTable() );
         }
      } );

   }

   public void clearTable()
   {
      cardLayout.show( this, "blank" );
      casesTableView = null;
      splitPane.setTopComponent( new JPanel() );
      clearCase();
   }

   public void clearCase()
   {
      detailsView.clear();
   }

   public CasesTableView getCaseTableView()
   {
      return casesTableView;
   }

   public CaseDetailView getCurrentCaseView()
   {
      return detailsView.getCurrentCaseView();
   }

   public void refresh()
   {
      if (casesTableView != null)
         casesTableView.getModel().refresh();
      detailsView.refresh();
   }

   public void setBlankPanel( JComponent blankPanel )
   {
      remove(blank);
      add( blank = blankPanel, "blank" );
   }

   class CaseSelectionListener
         implements ListSelectionListener
   {
      public void valueChanged( ListSelectionEvent e )
      {
         if (!e.getValueIsAdjusting())
         {
            final JTable caseTable = getCaseTableView().getCaseTable();

            SwingUtilities.invokeLater( new Runnable()
            {
               public void run()
               {

                  try
                  {
                     if (!caseTable.getSelectionModel().isSelectionEmpty())
                     {
                        int selectedRow = caseTable.getSelectedRow();
                        Object selectedValue = caseTable.getModel().getValueAt( caseTable.convertRowIndexToModel( selectedRow ), 9 );
                        if (selectedRow != -1 && !(selectedValue instanceof SeparatorList.Separator) )
                        {
                           String href = (String) selectedValue;
                           detailsView.show( casesModel.newCaseModel(href) );
                        }
                     } else
                     {
                        detailsView.selectCaseInTable( caseTable );
                     }
                  } catch (Exception e1)
                  {
                     throw new OperationException( CaseResources.could_not_view_details, e1 );
                  }
               }
            } );
         }
      }
   }
}

