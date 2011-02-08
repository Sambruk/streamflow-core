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

package se.streamsource.streamflow.client.ui.workspace.cases;

import static se.streamsource.streamflow.client.util.i18n.icon;
import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsView;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationsView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.FormsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseGeneralView;
import se.streamsource.streamflow.client.ui.workspace.cases.history.HistoryView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

/**
 * JAVADOC
 */
public class CaseDetailView
      extends JPanel
   implements TransactionListener
{
   private JTabbedPane tabs = new JTabbedPane( JTabbedPane.BOTTOM );
   private CaseModel model;
   private CaseInfoView caseInfo;
   private CaseActionsView caseActions;

   public CaseDetailView( @Uses CommandQueryClient client,
                          @Uses CaseModel model,
                          @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );
      this.model = model;
      this.setBorder( BorderFactory.createEtchedBorder() );
      tabs.setFocusable( true );

      add( caseInfo = obf.newObjectBuilder( CaseInfoView.class ).use( model ).newInstance(), BorderLayout.NORTH );
      add( caseActions = obf.newObjectBuilder( CaseActionsView.class ).use( model ).newInstance(), BorderLayout.EAST );

      // TODO This could be changed to use model.getResourceValue().resources()
      tabs.addTab( text( WorkspaceResources.general_tab ), icon( Icons.general ), obf.newObjectBuilder( CaseGeneralView.class ).use( client.getSubClient("general" )).newInstance(), text( WorkspaceResources.general_tab ) );
      tabs.addTab( text( WorkspaceResources.contacts_tab ), icon( Icons.projects ), obf.newObjectBuilder( ContactsAdminView.class ).use( client.getSubClient("contacts" )).newInstance(), text( WorkspaceResources.contacts_tab ) );
      tabs.addTab( text( WorkspaceResources.forms_tab ), icon( Icons.forms ), obf.newObjectBuilder( FormsAdminView.class ).use( client.getSubClient("submittedforms" )).newInstance(), text( WorkspaceResources.forms_tab ) );
      tabs.addTab( text( WorkspaceResources.conversations_tab ), icon( Icons.conversations ), obf.newObjectBuilder( ConversationsView.class ).use( client.getSubClient("conversations" )).newInstance(), text( WorkspaceResources.conversations_tab ) );
      tabs.addTab( text( WorkspaceResources.attachments_tab ), icon( Icons.attachments ), obf.newObjectBuilder( AttachmentsView.class ).use( client.getSubClient("attachments" )).newInstance(), text( WorkspaceResources.attachments_tab ) );
      tabs.addTab( text( WorkspaceResources.history_tab ), icon( Icons.history ), obf.newObjectBuilder( HistoryView.class ).use( client.getSubClient("history" )).newInstance(), text( WorkspaceResources.history_tab ) );
      
      tabs.setMnemonicAt( 0, KeyEvent.VK_1 );
      tabs.setMnemonicAt( 1, KeyEvent.VK_2 );
      tabs.setMnemonicAt( 2, KeyEvent.VK_3 );
      tabs.setMnemonicAt( 3, KeyEvent.VK_4 );
      tabs.setMnemonicAt( 4, KeyEvent.VK_5 );
      tabs.setMnemonicAt( 5, KeyEvent.VK_6 );

      tabs.setFocusable( true );
      tabs.setFocusCycleRoot( true );

      addFocusListener( new FocusListener()
      {
         public void focusGained( FocusEvent e )
         {
            tabs.getSelectedComponent().requestFocusInWindow();
         }

         public void focusLost( FocusEvent e )
         {
         }
      } );


      add( tabs, BorderLayout.CENTER );

      new RefreshWhenShowing( this, model );
   }

   public void setSelectedTab( int index )
   {
      if (tabs.getSelectedIndex() != index)
         tabs.setSelectedIndex( index );
   }

   public int getSelectedTab()
   {
      return tabs.getSelectedIndex();
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames( "changedOwner", "changedCaseType", "changedDescription", "assignedTo", "unassigned", "changedStatus" ), transactions ))
      {
         model.refresh();
      }
   }
}