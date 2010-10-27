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

package se.streamsource.streamflow.client.ui.administration.roles;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.LinkComparator;
import se.streamsource.streamflow.client.util.ListItemListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenVisible;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * JAVADOC
 */
public class RolesView
      extends JPanel
   implements TransactionListener
{
   RolesModel model;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   public JList roleList;

   public RolesView( @Service ApplicationContext context, @Uses final RolesModel model )
   {
      super( new BorderLayout() );
      this.model = model;

      setActionMap( context.getActionMap( this ) );

      roleList = new JList( new EventListModel<LinkValue>(new SortedList<LinkValue>(model.getList(), new LinkComparator())) );

      roleList.setCellRenderer( new ListItemListCellRenderer() );
      add( roleList, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( getActionMap().get( "add" ) ) );
      toolbar.add( new JButton( getActionMap().get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      new RefreshWhenVisible( this, model );
   }

   @Action
   public Task add()
   {
      NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_role_title ) );
      final String name = dialog.name();
      if ( Strings.notEmpty( name ) )
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
      final LinkValue selected = (LinkValue) roleList.getSelectedValue();

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
               model.remove(selected);
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      model.notifyTransactions( transactions);
   }
}