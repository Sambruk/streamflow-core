/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.administration.projectsettings;

import static se.streamsource.streamflow.client.util.i18n.text;

import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import ca.odell.glazedlists.swing.EventListModel;

import com.jgoodies.forms.factories.Borders;

/**
 * JAVADOC
 */
public class RecipientsView extends JPanel implements TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   public JXList recipientsList;
   private RecipientsModel model;

   public RecipientsView(@Service ApplicationContext context, @Uses final RecipientsModel model)
   {
      super( new BorderLayout() );
      this.model = model;
      
      setBorder( Borders.createEmptyBorder( "2dlu, 2dlu, 2dlu, 2dlu" ) );

      setActionMap( context.getActionMap( this ) );

      recipientsList = new JXList( new EventListModel<LinkValue>( model.getList() ) );

      recipientsList.setCellRenderer( new LinkListCellRenderer() );

      add( new JScrollPane( recipientsList ), BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new StreamflowButton( getActionMap().get( "add" ) ) );
      toolbar.add( new StreamflowButton( getActionMap().get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );
      recipientsList.getSelectionModel().addListSelectionListener(
            new SelectionActionEnabler( getActionMap().get( "remove" ) ) );

      new RefreshWhenShowing( this, model );
   }

   @Action
   public Task add()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder( SelectLinkDialog.class )
            .use( model.getPossibleRecipients() ).newInstance();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.dueon_notification_add_recipient ) );

      if (dialog.getSelectedLinks() != null && dialog.getSelectedLink() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command() throws Exception
            {
               model.addRecipient( dialog.getSelectedLink() );
            }
         };
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      final Iterable<LinkValue> selected = (Iterable) Iterables.iterable( recipientsList.getSelectedValues() );

      ConfirmationDialog dialog = module.objectBuilderFactory().newObject( ConfirmationDialog.class );
      String str = "";
      for (LinkValue linkValue : selected)
      {
         str += linkValue.text().get() + " ";
      }
      dialog.setRemovalMessage( str );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command() throws Exception
            {
               for (LinkValue linkValue : selected)
               {
                  model.removeRecipient( linkValue );
               }
            }
         };
      } else
         return null;
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      if (Events.matches( Events.withNames( "changedNotificationSettings"), transactions ))
      {
         model.refresh();
         recipientsList.clearSelection();
      }
   }

}