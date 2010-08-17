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

package se.streamsource.streamflow.client.ui.caze;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.caze.attachments.AttachmentsView;
import se.streamsource.streamflow.client.ui.caze.conversations.ConversationsView;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

/**
 * JAVADOC
 */
public class CaseDetailView
      extends JPanel
{
   private JTabbedPane tabs = new JTabbedPane( JTabbedPane.BOTTOM );

   private ConversationsView conversationsView;
   private CaseGeneralView generalView;
   private ContactsAdminView contactsView;
   private FormsAdminView formsView;
   private CaseModel model;
   private CaseInfoView infoView;
   private AttachmentsView attachmentsView;
   private CaseActionsView actionsView;

   public CaseDetailView( @Service ApplicationContext appContext,
                          @Uses CaseInfoView infoView,
                          @Uses CaseGeneralView generalView,
                          @Uses ConversationsView conversationsView,
                          @Uses ContactsAdminView contactsView,
                          @Uses FormsAdminView formsAdminView,
                          @Uses AttachmentsView attachmentsView,
                          @Uses CaseActionsView actionsView,
                          @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );
      this.infoView = infoView;
      this.infoView.setPreferredSize( new Dimension( 800, 50 ) );

      this.actionsView = actionsView;
      tabs.setFocusable( true );
      this.setBorder( BorderFactory.createEtchedBorder() );

      this.conversationsView = conversationsView;
      this.generalView = generalView;
      this.contactsView = contactsView;
      this.formsView = formsAdminView;
      this.attachmentsView = attachmentsView;

      tabs.addTab( i18n.text( WorkspaceResources.general_tab ), i18n.icon( Icons.general ), generalView, i18n.text( WorkspaceResources.general_tab ) );
      tabs.addTab( i18n.text( WorkspaceResources.contacts_tab ), i18n.icon( Icons.projects ), contactsView, i18n.text( WorkspaceResources.contacts_tab ) );
      tabs.addTab( i18n.text( WorkspaceResources.conversations_tab ), i18n.icon( Icons.comments ), conversationsView, i18n.text( WorkspaceResources.conversations_tab ) );
      tabs.addTab( i18n.text( WorkspaceResources.metadata_tab ), i18n.icon( Icons.metadata ), formsAdminView, i18n.text( WorkspaceResources.metadata_tab ) );
      tabs.addTab( i18n.text( WorkspaceResources.attachments_tab ), i18n.icon( Icons.attachments ), attachmentsView, i18n.text( WorkspaceResources.attachments_tab ) );

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

      add( infoView, BorderLayout.NORTH );

      add( tabs, BorderLayout.CENTER );

      add( actionsView, BorderLayout.EAST );
   }

   public void setCaseModel( CaseModel model )
   {
      if (model != this.model)
      {
         this.model = model;
         infoView.setModel( model.info() );
         generalView.setModel( model.general() );
         conversationsView.setModel( model.conversations() );
         contactsView.setModel( model.contacts() );
         formsView.setModel( model.forms() );
         attachmentsView.setModel( model.attachments() );

         actionsView.setModel( model.actions() );
         actionsView.refresh();

         validateTree();
      }
   }

   public CaseModel getCaseModel()
   {
      return model;
   }

   public void setSelectedTab( int index )
   {
      tabs.setSelectedIndex( index );
   }
}