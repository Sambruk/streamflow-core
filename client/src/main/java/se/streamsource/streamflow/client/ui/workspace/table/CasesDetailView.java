/*
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

package se.streamsource.streamflow.client.ui.workspace.table;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.data.Reference;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseDetailView;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

/**
 * JAVADOC
 */
public class CasesDetailView
      extends JPanel
   implements TransactionListener
{
   private CaseDetailView current = null;

   @Structure
   ObjectBuilderFactory obf;

   private CardLayout layout = new CardLayout();
   private JPanel casePanel = new JPanel(new BorderLayout());

   private Reference currentCase;

   public CasesDetailView( )
   {
      setLayout( layout );
      setBorder( BorderFactory.createEmptyBorder() );

      add( new JLabel( i18n.text( WorkspaceResources.choose_case ), JLabel.CENTER ), "blank" );
      add( casePanel, "detail" );

      layout.show( this, "blank" );

      setPreferredSize( new Dimension( getWidth(), 500 ) );
   }

   public void show( CommandQueryClient client)
   {
      if (currentCase == null || !currentCase.equals( client.getReference() ))
      {
         if (current != null)
         {
            int tab = current.getSelectedTab();
            currentCase = client.getReference();
            casePanel.removeAll();
            current = obf.newObjectBuilder( CaseDetailView.class ).use( client ).newInstance();
            current.setSelectedTab( tab );
            casePanel.add( current );
            casePanel.revalidate();
         } else
         {
            currentCase = client.getReference();
            casePanel.add(current = obf.newObjectBuilder( CaseDetailView.class ).use( client ).newInstance());
            layout.show( this, "detail" );

         }
      }
   }

   public void clear()
   {
      layout.show( this, "blank" );
      casePanel.removeAll();
      casePanel.revalidate();
      current = null;
   }

   @Override
   public boolean requestFocusInWindow()
   {
      return current == null ? false : current.requestFocusInWindow();
   }

   public CaseDetailView getCurrentCaseView()
   {
      return current;
   }

   public void refresh()
   {
      if (current != null)
      {
         layout.show( this, "blank" );
         layout.show( this, "detail" );
      }
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( Events.withNames("deletedEntity" ), transactions ))
         clear();
   }
}