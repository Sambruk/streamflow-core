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

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceView;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseDetailView;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseModel;
import se.streamsource.streamflow.client.ui.workspace.cases.CasesModel;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;


/**
 * JAVADOC
 */
public class CasesDetailView
      extends JPanel
      implements TransactionListener
{
   @Structure
   Module module;

   @Uses
   private CasesModel casesModel;

   private CaseDetailView currentView = null;

   private CardLayout layout = new CardLayout();
   private JPanel casePanel = new JPanel( new BorderLayout(  ));

   private CaseModel currentCase;

   public CasesDetailView(@Service ApplicationContext context)
   {
      setLayout( layout );
      setBorder( BorderFactory.createEmptyBorder() );

      setActionMap( context.getActionMap(this) );

      add( new JLabel( i18n.text( WorkspaceResources.choose_case ), JLabel.CENTER ), "blank" );
      add( casePanel, "detail" );

      layout.show( this, "blank" );

      setPreferredSize( new Dimension( getWidth(), 500 ) );
   }

   public void show( final CaseModel model )
   {
      if (currentCase == null || !currentCase.equals( model ))
      {
         if (currentView != null)
         {
            int tab = currentView.getSelectedTab();
            currentCase = model;
            currentView = module.objectBuilderFactory().newObjectBuilder(CaseDetailView.class).use( model ).newInstance();
            currentView.setSelectedTab( tab );
            casePanel.add( currentView, BorderLayout.CENTER );
         } else
         {
            currentCase = model;
            currentView = module.objectBuilderFactory().newObjectBuilder(CaseDetailView.class).use( model ).newInstance();
            casePanel.add( currentView, BorderLayout.CENTER );
            layout.show( this, "detail" );
         }
         currentView.requestFocusInWindow();
      }
   }

   public void clear()
   {
      layout.show( this, "blank" );
      casePanel.removeAll();
      currentView = null;
      currentCase = null;
   }

   @Override
   public boolean requestFocusInWindow()
   {
      return currentView != null && currentView.requestFocusInWindow();
   }

   public CaseDetailView getCurrentCaseView()
   {
      return currentView;
   }

   public void refresh()
   {
      if (currentView != null)
      {
         layout.show( this, "blank" );
         layout.show( this, "detail" );
      }
   }


   /**
    * Selects this case in a table if the case is available.
    * If not the case detail is cleared.
    * @param cases A JTable containing cases.
    */
   public void selectCaseInTable( final JTable cases )
   {
      if( currentCase != null )
      {
         TableModel model = cases.getModel();
         boolean rowFound = false;
         for( int i=0, n=model.getRowCount(); i < n; i++ )
         {
            if( currentCase.toString().endsWith( model.getValueAt( i, model.getColumnCount() ).toString() ) )
            {
               cases.getSelectionModel().setSelectionInterval( cases.convertRowIndexToView( i ), cases.convertRowIndexToView( i )  );
               cases.scrollRectToVisible( cases.getCellRect( i, 0, true ) );
               rowFound = true;
               break;
            }
         }
         if( !rowFound )
         {
            WorkspaceView workspace = (WorkspaceView)SwingUtilities.getAncestorOfClass( WorkspaceView.class, this );
            // if overview - always close detail on changes
            if( workspace == null || !workspace.getWorkspaceContext().showContext( currentCase ) )
               clear();
         }
      }
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (currentCase != null)
      {
         if (matches( onEntityTypes( "se.streamsource.streamflow.web.domain.entity.caze.CaseEntity" ), transactions ))
         {
            if (matches( withNames( "changedRemoved", "createdCase" ), transactions ))
            {
               clear();
            }
            // clear detail if status changed from draft to open
            else if (matches( withUsecases( "open" ), transactions ) )
            {
               clear();
            }
         }
      }
   }
}