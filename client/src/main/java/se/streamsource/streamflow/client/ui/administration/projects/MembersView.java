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

package se.streamsource.streamflow.client.ui.administration.projects;

import ca.odell.glazedlists.swing.*;
import com.jgoodies.forms.factories.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.jdesktop.swingx.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import org.qi4j.api.util.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.ui.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * JAVADOC
 */
public class MembersView
      extends JPanel
   implements TransactionListener
{
   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Uses
   ObjectBuilder<SelectUsersAndGroupsDialog> selectUsersAndGroups;

   private UsersAndGroupsModel usersAndGroupsModel;

   public JXList membersList;
   private MembersModel membersModel;

   public MembersView( @Service ApplicationContext context,
                              @Uses final CommandQueryClient client,
                              @Structure ObjectBuilderFactory obf)
   {
      super( new BorderLayout() );
      this.membersModel = obf.newObjectBuilder( MembersModel.class ).use( client ).newInstance();
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      usersAndGroupsModel = obf.newObjectBuilder( UsersAndGroupsModel.class ).use( client ).newInstance();
      setActionMap( context.getActionMap( this ) );

      membersList = new JXList( new EventListModel<LinkValue>(membersModel.getList()) );

      membersList.setCellRenderer( new LinkListCellRenderer() );

      add( new JScrollPane(membersList), BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( getActionMap().get( "add" ) ) );
      toolbar.add( new JButton( getActionMap().get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );
      membersList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( getActionMap().get( "remove" ) ) );


      new RefreshWhenShowing( this, membersModel );
   }


   @Action
   public Task add()
   {
      SelectUsersAndGroupsDialog dialog = selectUsersAndGroups.use( usersAndGroupsModel ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text(AdministrationResources.add_user_or_group_title) );

      final Set<LinkValue> linkValueSet = dialog.getSelectedEntities();
      if ( !linkValueSet.isEmpty() )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               membersModel.add( linkValueSet );
            }
         };
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      final Iterable<LinkValue> selected = (Iterable) Iterables.iterable( membersList.getSelectedValues() );

      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      String str = "";
      for (LinkValue linkValue : selected)
      {
         str += linkValue.text().get()+" ";
      }
      dialog.setRemovalMessage( str );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed() )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               for (LinkValue linkValue : selected)
               {
                  membersModel.remove( linkValue );
               }
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      membersModel.notifyTransactions(transactions);
   }
}