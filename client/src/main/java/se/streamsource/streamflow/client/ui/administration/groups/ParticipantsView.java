/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.administration.groups;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.SelectUsersAndGroupsDialog;
import se.streamsource.dci.value.LinkValue;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.Set;

/**
 * JAVADOC
 */
public class ParticipantsView
      extends JPanel
{
   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Uses
   ObjectBuilder<SelectUsersAndGroupsDialog> selectUsersAndGroups;

   public JList participantList;

   private ParticipantsModel model;

   public ParticipantsView( @Service ApplicationContext context, @Uses ParticipantsModel model )
   {
      super( new BorderLayout() );
      this.model = model;

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      participantList = new JList( new EventListModel<LinkValue>(new SortedList<LinkValue>(model.getParticipants(), new LinkComparator())) );

      participantList.setCellRenderer( new LinkListCellRenderer() );

      add( participantList, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );
   }

   @Action
   public void add() throws ResourceException
   {
      SelectUsersAndGroupsDialog dialog = selectUsersAndGroups.use( model.getClient() ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog );
      Set<String> participants = dialog.getUsersAndGroups();
      if (participants != null)
      {
         model.addParticipants( participants );
         model.refresh();
      }
   }

   @Action
   public void remove() throws ResourceException
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamFlowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         LinkValue value = (LinkValue) participantList.getSelectedValue();
         model.removeParticipant( value.id().get() );
         model.refresh();
      }
   }

   public JList getParticipantList()
   {
      return participantList;
   }
}