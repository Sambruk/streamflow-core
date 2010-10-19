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

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.CommandTask;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.ListDetailView;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.surface.AccessPointView;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.util.Strings;

import javax.swing.ActionMap;
import java.awt.Component;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

/**
 * JAVADOC
 */
public class GroupsView
      extends ListDetailView
{
   @Uses
   private Iterable<NameDialog> nameDialogs;

   @Uses
   private Iterable<ConfirmationDialog> confirmationDialog;

   private GroupsModel model;

   @Service
   private DialogService dialogs;

   public GroupsView( @Service ApplicationContext context, @Uses final CommandQueryClient client, @Structure final ObjectBuilderFactory obf)
   {
      this.model = obf.newObjectBuilder( GroupsModel.class ).use( client ).newInstance();

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      initMaster( new EventListModel<LinkValue>( model.getList()), am.get("add"), new javax.swing.Action[]{am.get( "rename" ), am.get( "remove" )}, new DetailFactory()
      {
         public Component createDetail( LinkValue detailLink )
         {
            CommandQueryClient caseTypeClient = client.getClient( detailLink );
            return obf.newObjectBuilder( AccessPointView.class ).use( caseTypeClient).newInstance();
         }
      });

      new RefreshWhenVisible(this, model);
   }

   @Action
   public Task add()
   {
      NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_group_title ) );
      final String name = dialog.name();
      if (Strings.notEmpty( name ))
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.create( name );
            }
         };
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();

      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialog.setRemovalMessage( selected.text().get() );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.remove( selected );
            }
         };
      } else
         return null;
   }

   @Action
   public Task rename()
   {
      final NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.rename_group_title ) );

      if (Strings.notEmpty( dialog.name() ) )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeDescription( (LinkValue)list.getSelectedValue(), dialog.name() );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      model.notifyTransactions( transactions );
   }
}
