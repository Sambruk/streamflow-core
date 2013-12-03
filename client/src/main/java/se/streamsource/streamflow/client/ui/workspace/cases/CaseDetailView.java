/**
 *
 * Copyright 2009-2013 Jayway Products AB
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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsView;
import se.streamsource.streamflow.client.ui.workspace.cases.caselog.CaseLogView;
import se.streamsource.streamflow.client.ui.workspace.cases.contacts.ContactsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.conversations.ConversationsView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.SubmittedFormsAdminView;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseGeneralView;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * JAVADOC
 */
public class CaseDetailView
      extends JPanel
   implements Observer, TransactionListener, HierarchyListener
{
   private JTabbedPane tabs = new JTabbedPane( JTabbedPane.BOTTOM );
   private CaseModel model;
   private CaseInfoView caseInfo;
   private CaseActionsView caseActions;

   private Timer timer;
   private TimerTask timerTask;

   public CaseDetailView( @Uses CaseModel model,
                          @Structure Module module)
   {
      super( new BorderLayout() );
      this.model = model;
      model.addObserver( this );
      this.setBorder( BorderFactory.createEtchedBorder() );
      tabs.setFocusable( true );

      add( caseInfo = module.objectBuilderFactory().newObjectBuilder(CaseInfoView.class).use( model ).newInstance(), BorderLayout.NORTH );
      add( caseActions = module.objectBuilderFactory().newObjectBuilder(CaseActionsView.class).use( model ).newInstance(), BorderLayout.EAST );

      // CaseLogView
      CaseLogView caseLogView = module.objectBuilderFactory().newObjectBuilder( CaseLogView.class ).use( model.newCaseLogModel() ).newInstance();
      
      // TODO This could be changed to use model.getResourceValue().resources()
      tabs.addTab( text( WorkspaceResources.general_tab ), icon( Icons.general ), module.objectBuilderFactory().newObjectBuilder(CaseGeneralView.class).use( model.newGeneralModel(), caseLogView).newInstance(), text( WorkspaceResources.general_tab ) );
      tabs.addTab( text( WorkspaceResources.contacts_tab ), icon( Icons.projects ), module.objectBuilderFactory().newObjectBuilder(ContactsAdminView.class).use( model.newContactsModel()).newInstance(), text( WorkspaceResources.contacts_tab ) );
      tabs.addTab( text( WorkspaceResources.forms_tab ), icon( Icons.forms ), module.objectBuilderFactory().newObjectBuilder(SubmittedFormsAdminView.class).use( model.newSubmittedFormsModel()).newInstance(), text( WorkspaceResources.forms_tab ) );
      tabs.addTab( text( WorkspaceResources.conversations_tab ), icon( Icons.conversations ), module.objectBuilderFactory().newObjectBuilder(ConversationsView.class).use( model.newConversationsModel()).newInstance(), text( WorkspaceResources.conversations_tab ) );
      tabs.addTab( text( WorkspaceResources.attachments_tab ), icon( Icons.attachments ), module.objectBuilderFactory().newObjectBuilder(AttachmentsView.class).use(model.newAttachmentsModel()).newInstance(), text( WorkspaceResources.attachments_tab ) );
      
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

      addAncestorListener ( new AncestorListener()
      {
         public void ancestorAdded ( AncestorEvent event )
         {
            // Component added somewhere
         }

         public void ancestorRemoved ( AncestorEvent event )
         {
            if(timerTask != null )
            {
               timerTask.cancel();
               timer.cancel();
               timerTask = null;
               timer = null;
            }
         }

         public void ancestorMoved ( AncestorEvent event )
         {
            // Component container moved
         }
      } );

      add( tabs, BorderLayout.CENTER );

      new RefreshWhenShowing( this, model );

      addHierarchyListener( this );

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
      if (Events.matches( Events.withNames( "changedOwner", "changedCaseType", "changedDescription", "assignedTo", "unassigned", "changedStatus", "createdMessageFromDraft", "submittedForm", "setUnread" ), transactions ))
      {
         model.refresh();
      }
   }

   public void update( Observable o, Object arg )
   {
      tabs.setIconAt( 2, model.getIndex().hasUnreadForm().get() ? icon( Icons.unreadforms )  : icon( Icons.forms ) );
      tabs.setIconAt( 3, model.getIndex().hasUnreadConversation().get() ? icon( Icons.unreadconversations )  : icon( Icons.conversations ) );
   }

   public void hierarchyChanged( HierarchyEvent e )
   {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) > 0 && CaseDetailView.this.isShowing())
      {
         if( model.getIndex() == null )
         {
            model.refresh();
         }
         if (model.getIndex().unread().get())
         {

            timer = new Timer();
            timer.schedule(
               timerTask = new TimerTask()
               {

                  @Override
                  public void run()
                  {
                     new CommandTask()
                     {
                        @Override
                        protected void command() throws Exception
                        {
                           model.read();
                        }
                     }.execute();
                  }
               }, ((StreamflowApplication) StreamflowApplication.getInstance()).markReadTimeout() );
         }
      }
   }
}