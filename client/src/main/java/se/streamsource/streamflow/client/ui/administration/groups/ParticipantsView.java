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

package se.streamsource.streamflow.client.ui.administration.groups;

import java.awt.BorderLayout;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.SelectUsersAndGroupsDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.UsersAndGroupsModel;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;

import com.jgoodies.forms.factories.Borders;

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

   @Uses
   ObjectBuilder<UsersAndGroupsModel> usersAndGroupsModel;

   public JList participantList;

   private ParticipantsModel model;

   public ParticipantsView( @Service ApplicationContext context,
                            @Uses ParticipantsModel model,
                            @Structure ObjectBuilderFactory obf)
   {
      super( new BorderLayout() );
      this.model = model;
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      participantList = new JList( new EventListModel<LinkValue>(new SortedList<LinkValue>(model.getParticipants(), new LinkComparator())) );

      participantList.setCellRenderer( new LinkListCellRenderer() );

      add( new JScrollPane(participantList), BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );
   }

   @Action
   public void add() throws ResourceException
   {
      UsersAndGroupsModel dialogModel = usersAndGroupsModel.use( model.getClient() ).newInstance();
      SelectUsersAndGroupsDialog dialog = selectUsersAndGroups.use( dialogModel ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text(AdministrationResources.add_user_or_group_title) );

      Set<LinkValue> linkValueSet = dialog.getSelectedEntities();
      if ( !linkValueSet.isEmpty() )
      {
         model.addParticipants( linkValueSet );
         model.refresh();
      }
   }

   @Action
   public void remove() throws ResourceException
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
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