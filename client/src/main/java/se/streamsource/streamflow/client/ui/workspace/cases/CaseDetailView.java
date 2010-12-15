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

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.actions.CaseActionsView;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsView;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationsView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.FormsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseGeneralView;
import se.streamsource.streamflow.client.ui.workspace.cases.info.CaseInfoView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import static se.streamsource.streamflow.client.util.i18n.icon;
import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * JAVADOC
 */
public class CaseDetailView
      extends JPanel
{
   private JTabbedPane tabs = new JTabbedPane( JTabbedPane.BOTTOM );
   private CaseInfoView caseInfo;
   private CaseActionsView caseActions;

   public CaseDetailView( @Service ApplicationContext appContext,
                          @Uses CommandQueryClient client,
                          @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );
      this.setBorder( BorderFactory.createEtchedBorder() );
      tabs.setFocusable( true );

      add( caseInfo = obf.newObjectBuilder( CaseInfoView.class ).use( client ).newInstance(), BorderLayout.NORTH );
      add( caseActions = obf.newObjectBuilder( CaseActionsView.class ).use( client ).newInstance(), BorderLayout.EAST );

      tabs.addTab( text( WorkspaceResources.general_tab ), icon( Icons.general ), obf.newObjectBuilder( CaseGeneralView.class ).use( client.getSubClient("general" )).newInstance(), text( WorkspaceResources.general_tab ) );
      tabs.addTab( text( WorkspaceResources.contacts_tab ), icon( Icons.projects ), obf.newObjectBuilder( ContactsAdminView.class ).use( client.getSubClient("contacts" )).newInstance(), text( WorkspaceResources.contacts_tab ) );
      tabs.addTab( text( WorkspaceResources.conversations_tab ), icon( Icons.conversations ), obf.newObjectBuilder( ConversationsView.class ).use( client.getSubClient("conversations" )).newInstance(), text( WorkspaceResources.conversations_tab ) );
      tabs.addTab( text( WorkspaceResources.forms_tab ), icon( Icons.forms ), obf.newObjectBuilder( FormsAdminView.class ).use( client.getSubClient("submittedforms" )).newInstance(), text( WorkspaceResources.forms_tab ) );
      tabs.addTab( text( WorkspaceResources.attachments_tab ), icon( Icons.attachments ), obf.newObjectBuilder( AttachmentsView.class ).use( client.getSubClient("attachments" )).newInstance(), text( WorkspaceResources.attachments_tab ) );

      tabs.setMnemonicAt( 0, KeyEvent.VK_1 );
      tabs.setMnemonicAt( 1, KeyEvent.VK_2 );
      tabs.setMnemonicAt( 2, KeyEvent.VK_3 );
      tabs.setMnemonicAt( 3, KeyEvent.VK_4 );
      tabs.setMnemonicAt( 4, KeyEvent.VK_5 );

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

   public String getCaseStatus()
   {
      return caseInfo.getCaseStatus();
   }

   public CaseInfoView getCaseInfo()
   {
      return caseInfo;
   }
}