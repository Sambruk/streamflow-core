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

package se.streamsource.streamflow.client.ui.administration.organizations;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.util.Iterables;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinksDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.util.List;

import static se.streamsource.streamflow.client.util.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

public class OrganizationUsersView
      extends JPanel
      implements TransactionListener
{
   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Structure
   ObjectBuilderFactory obf;
   @Service
   DialogService dialogs;

   public JList participantList;

   private OrganizationUsersModel model;
   public EventList<LinkValue> linkValues;

   public OrganizationUsersView( @Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );
      this.model = obf.newObjectBuilder( OrganizationUsersModel.class ).use( client ).newInstance();

      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      linkValues = model.getList();
      participantList = new JList( new EventListModel<LinkValue>( linkValues ) );
      participantList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

      participantList.setCellRenderer( new LinkListCellRenderer() );

      JScrollPane scrollPane = new JScrollPane( participantList );
      add( scrollPane, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      participantList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );

      new RefreshWhenShowing( this, model );
   }

   @org.jdesktop.application.Action
   public Task add() throws ResourceException
   {
      final SelectLinksDialog dialog = obf.newObjectBuilder( SelectLinksDialog.class )
            .use( model.getPossible() ).newInstance();
      dialogs.showOkCancelHelpDialog(
            WindowUtils.findWindow( this ),
            dialog,
            text( AdministrationResources.join_organization ) );

      if (dialog.getSelectedLinks() != null)
      {
         final List<LinkValue> links = dialog.getSelectedLinks().links().get();

         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.add( links );
            }
         };
      } else
         return null;
   }

   @org.jdesktop.application.Action
   public Task remove() throws ResourceException
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialog.setRemovalMessage( i18n.text( AdministrationResources.users_tab) );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         final Iterable<LinkValue> selected = (Iterable) Iterables.iterable( participantList.getSelectedValues() );
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               for (LinkValue linkValue : selected)
               {
                  model.remove( linkValue );
               }
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( Events.withNames( "joinedOrganization", "leftOrganization" ), transactions ))
         model.refresh();
   }
}
