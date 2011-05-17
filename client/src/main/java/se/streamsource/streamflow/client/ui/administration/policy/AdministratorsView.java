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

package se.streamsource.streamflow.client.ui.administration.policy;

import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.ui.SelectUsersAndGroupsDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.UsersAndGroupsModel;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * JAVADOC
 */
public class AdministratorsView
      extends JPanel
   implements TransactionListener
{
   AdministratorsModel model;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Uses
   ObjectBuilder<SelectUsersAndGroupsDialog> selectUsersAndGroupsDialogs;
   private UsersAndGroupsModel usersAndGroupsModel;

   public JList administratorList;

   public AdministratorsView( @Service ApplicationContext context,
                              @Uses AdministratorsModel model,
                              @Structure ObjectBuilderFactory obf)
   {
      super( new BorderLayout() );
      this.model = model;
      setBorder(Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      usersAndGroupsModel = model.newUsersAndGroupsModel();

      setActionMap( context.getActionMap( this ) );

      administratorList = new JList( new EventListModel<LinkValue>(model.getList()) );

      administratorList.setCellRenderer( new LinkListCellRenderer() );
      add( new JScrollPane(administratorList), BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( getActionMap().get( "add" ) ) );
      toolbar.add( new JButton( getActionMap().get( "remove" ) ) );
      add( toolbar, BorderLayout.SOUTH );

      new RefreshWhenShowing( this, model );
   }

   @Action
   public Task add()
   {
      SelectUsersAndGroupsDialog dialog = selectUsersAndGroupsDialogs.use( usersAndGroupsModel ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_user_or_group_title ) );

      final Set<LinkValue> linkValueSet = dialog.getSelectedEntities();
      if ( !linkValueSet.isEmpty() )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               for (LinkValue identity : linkValueSet)
               {
                  model.addAdministrator( identity );
               }
            }
         };
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      final Iterable<LinkValue> selected = (Iterable) Iterables.iterable( administratorList.getSelectedValues() );

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
      model.notifyTransactions( transactions );
   }
}