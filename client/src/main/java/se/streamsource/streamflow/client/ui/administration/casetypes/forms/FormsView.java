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

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.CommandTask;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.ListDetailView;
import se.streamsource.streamflow.client.ui.NameDialog;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.TabbedResourceView;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectionDialog;
import ca.odell.glazedlists.swing.EventListModel;

import com.jgoodies.forms.factories.Borders;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;

/**
 * JAVADOC
 */
public class FormsView
      extends ListDetailView
{
   private FormsModel model;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Uses
   ObjectBuilder<SelectionDialog> possibleMoveToDialogs;

   public FormsView( @Service ApplicationContext context, @Uses final CommandQueryClient client, @Structure final ObjectBuilderFactory obf)
   {
      this.model = obf.newObjectBuilder( FormsModel.class ).use( client ).newInstance();

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      initMaster( new EventListModel<LinkValue>( model.getList()), am.get("add"), new javax.swing.Action[]{am.get( "move" ), am.get("remove")}, new DetailFactory()
      {
         public Component createDetail( LinkValue detailLink )
         {
            CommandQueryClient formClient = client.getClient( detailLink );

            return obf.newObjectBuilder( FormView.class ).use( formClient).newInstance();
         }
      });

      new RefreshWhenVisible(this, model);
   }

   @Action
   public Task add()
   {
      NameDialog formDialog = nameDialogs.iterator().next();

      dialogs.showOkCancelHelpDialog( this, formDialog, i18n.text( AdministrationResources.create_new_form ) );

      final String name = formDialog.name();
      if (Strings.notEmpty( name ) )
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
         if (selected != null)
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
         }
      }

      return null;
   }

   @Action
   public Task move()
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();
      final SelectionDialog dialog = possibleMoveToDialogs.use(model.getPossibleMoveTo()).newInstance();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_move_to ) );

      if (dialog.getSelectedLinks() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.moveForm(selected, dialog.getSelectedLink());
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