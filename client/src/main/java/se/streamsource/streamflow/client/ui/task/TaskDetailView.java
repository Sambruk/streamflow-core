/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.task;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

/**
 * JAVADOC
 */
public class TaskDetailView
      extends JPanel
{
   private JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);

   private TaskCommentsView commentsView;
   private TaskGeneralView generalView;
   private TaskContactsAdminView contactsView;
   private TaskFormsAdminView formsView;
   private TaskModel model;
   private TaskInfoView infoView;
   private TaskActionsView actionsView;

   public TaskDetailView( @Service ApplicationContext appContext,
                          @Uses TaskInfoView infoView,
                          @Uses TaskGeneralView generalView,
                          @Uses TaskCommentsView commentsView,
                          @Uses TaskContactsAdminView contactsView,
                          @Uses TaskFormsAdminView formsAdminView,
                          @Uses TaskActionsView actionsView,
                          @Structure ObjectBuilderFactory obf )
   {
      super(new BorderLayout());
      this.infoView = infoView;

      this.actionsView = actionsView;
      tabs.setFocusable( true );

      this.commentsView = commentsView;
      this.generalView = generalView;
      this.contactsView = contactsView;
      this.formsView = formsAdminView;

      tabs.addTab( i18n.text( WorkspaceResources.general_tab ), i18n.icon( Icons.general ), generalView, i18n.text( WorkspaceResources.general_tab ) );
      tabs.addTab( i18n.text( WorkspaceResources.contacts_tab ), i18n.icon( Icons.projects ), contactsView, i18n.text( WorkspaceResources.contacts_tab ) );
      tabs.addTab( i18n.text( WorkspaceResources.comments_tab ), i18n.icon( Icons.comments ), commentsView, i18n.text( WorkspaceResources.comments_tab ) );
      tabs.addTab( i18n.text( WorkspaceResources.metadata_tab ), i18n.icon( Icons.metadata ), formsAdminView, i18n.text( WorkspaceResources.metadata_tab ) );
      tabs.addTab( i18n.text( WorkspaceResources.attachments_tab ), i18n.icon( Icons.attachments ), new JLabel( "Attachments" ), i18n.text( WorkspaceResources.attachments_tab ) );

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

      add(infoView, BorderLayout.NORTH);

      add(tabs, BorderLayout.CENTER);

      add( actionsView, BorderLayout.EAST);
   }

   public void setTaskModel( TaskModel model )
   {
      this.model = model;
      infoView.setModel( model.info() );
      generalView.setModel( model.general() );
      commentsView.setModel( model.comments() );
      contactsView.setModel( model.contacts() );
      formsView.setModel( model.forms() );

      actionsView.setModel(model.actions());
      actionsView.refresh();

      validateTree();
   }

   public TaskModel getTaskModel()
   {
      return model;
   }
}