/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration.groups;

import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ui.SelectUsersAndGroupsDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.UsersAndGroupsModel;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.ActionMap;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.util.Set;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * JAVADOC
 */
public class ParticipantsView
      extends JPanel
   implements TransactionListener
{
   @Service
   DialogService dialogs;

   @Structure
   Module module;

   public JList participantList;

   private ParticipantsModel model;

   public ParticipantsView( @Service ApplicationContext context,
                            @Uses final ParticipantsModel model)
   {
      super( new BorderLayout() );
      this.model = model;
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      participantList = new JList( new EventListModel<LinkValue>(model.getList()) );

      participantList.setCellRenderer( new LinkListCellRenderer() );

      add( new JScrollPane(participantList), BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new StreamflowButton( am.get( "add" ) ) );
      toolbar.add( new StreamflowButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      participantList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "remove" ) ) );

      new RefreshWhenShowing(this, model);
   }

   @Action
   public Task add() throws ResourceException
   {
      UsersAndGroupsModel dialogModel = model.newUsersAndGroupsModel();
      SelectUsersAndGroupsDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectUsersAndGroupsDialog.class).use( dialogModel ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text(AdministrationResources.add_user_or_group_title) );

      final Set<LinkValue> linkValueSet = dialog.getSelectedEntities();
      return new CommandTask()
      {
         @Override
         public void command()
            throws Exception
         {
            model.add( linkValueSet );
         }
      };
   }

   @Action
   public Task remove() throws ResourceException
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
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if( matches( withNames( "addedParticipant", "removedParticipant"), transactions )
         && !matches( withNames( "removedGroup" ),transactions) )
      {
        model.notifyTransactions( transactions );
      }
   }
}