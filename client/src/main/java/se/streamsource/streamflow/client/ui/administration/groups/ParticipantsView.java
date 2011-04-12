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

package se.streamsource.streamflow.client.ui.administration.groups;

import ca.odell.glazedlists.swing.*;
import com.jgoodies.forms.factories.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import org.qi4j.api.util.*;
import org.restlet.resource.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.ui.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static org.qi4j.api.specification.Specifications.*;
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

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Uses
   ObjectBuilder<SelectUsersAndGroupsDialog> selectUsersAndGroups;

   @Uses
   ObjectBuilder<UsersAndGroupsModel> usersAndGroupsModel;

   public JList participantList;

   private ParticipantsModel model;
   private final CommandQueryClient client;

   public ParticipantsView( @Service ApplicationContext context,
                            @Uses CommandQueryClient client,
                            @Structure ObjectBuilderFactory obf)
   {
      super( new BorderLayout() );
      this.client = client;
      this.model = obf.newObjectBuilder( ParticipantsModel.class ).use(client).newInstance();
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      participantList = new JList( new EventListModel<LinkValue>(model.getList()) );

      participantList.setCellRenderer( new LinkListCellRenderer() );

      add( new JScrollPane(participantList), BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( am.get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      new RefreshWhenShowing(this, model);
   }

   @Action
   public Task add() throws ResourceException
   {
      UsersAndGroupsModel dialogModel = usersAndGroupsModel.use( client ).newInstance();
      SelectUsersAndGroupsDialog dialog = selectUsersAndGroups.use( dialogModel ).newInstance();
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

      ConfirmationDialog dialog = confirmationDialog.iterator().next();
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
      if( matches( not( withNames( "removedGroup", "changedRemoved") ), transactions ) )
         model.notifyTransactions( transactions );
   }
}