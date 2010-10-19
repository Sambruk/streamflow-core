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

package se.streamsource.streamflow.client.ui.administration.surface;

import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.CommandTask;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.CreateProxyUserDialog;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.ResetPasswordDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.resource.user.ProxyUserDTO;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.source.helper.Events.*;

public class ProxyUsersView
      extends JPanel
   implements TransactionListener
{
   private ProxyUsersModel model;

   @Uses
   Iterable<CreateProxyUserDialog> userDialogs;

   @Uses
   Iterable<ResetPasswordDialog> resetPwdDialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Service
   DialogService dialogs;

   JXTable proxyUsersTable;
   private EventJXTableModel<ProxyUserDTO> tableModel;

   public ProxyUsersView( @Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf)
   {
      ApplicationActionMap am = context.getActionMap( this );
      setActionMap( am );

      this.model = obf.newObjectBuilder( ProxyUsersModel.class ).use( client ).newInstance();
      TableFormat<ProxyUserDTO> tableFormat = new ProxyUsersTableFormat();

      tableModel = new EventJXTableModel<ProxyUserDTO>( model.getEventList(), tableFormat );

      proxyUsersTable = new JXTable( tableModel );
      proxyUsersTable.getColumn( 0 ).setCellRenderer( new DefaultTableRenderer( new CheckBoxProvider() ) );
      proxyUsersTable.getColumn( 0 ).setMaxWidth( 30 );
      proxyUsersTable.getColumn( 0 ).setResizable( false );
      proxyUsersTable.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "resetPassword" ) ) );

      JScrollPane scroll = new JScrollPane();
      scroll.setViewportView( proxyUsersTable );

      super.setLayout(new BorderLayout());
      super.add( scroll, BorderLayout.CENTER );

      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "resetPassword" ) );
      options.add( am.get( "remove" ) );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( new OptionsAction( options ) ) );
      super.add( toolbar, BorderLayout.SOUTH );

      proxyUsersTable.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "resetPassword" ), am.get( "remove") ) );
      new RefreshWhenVisible(this, model);
   }


   @org.jdesktop.application.Action
   public Task add()
   {
      final CreateProxyUserDialog dialog = userDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.create_user_title ) );

      if ( dialog.userCommand() != null )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.createProxyUser( dialog.userCommand() );
            }
         };
      } else
         return null;
   }

   @org.jdesktop.application.Action
   public Task resetPassword()
   {
      final ResetPasswordDialog dialog = resetPwdDialogs.iterator().next();

      final ProxyUserDTO proxyUser = tableModel.getElementAt( proxyUsersTable.getSelectedRow() );

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.reset_password_title ) + ": " + proxyUser.description().get() );

      if (dialog.password() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.resetPassword( proxyUser, dialog.password() );
            }
         };
      } else
         return null;
   }

      @Action
   public Task remove()
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      final ProxyUserDTO proxyUser = tableModel.getElementAt( proxyUsersTable.getSelectedRow() );
      dialog.setRemovalMessage( proxyUser.description().get() );
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.remove_proxyuser_title ) );

      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.remove( proxyUser );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (matches( transactions, withNames("createdProxyUser", "changedEnabled" )))
      {
         model.refresh();
      }
   }

   private class ProxyUsersTableFormat
      implements WritableTableFormat<ProxyUserDTO>
   {
      public boolean isEditable( ProxyUserDTO proxyUserDTO, int i )
      {
         return i == 0;
      }

      public ProxyUserDTO setColumnValue( ProxyUserDTO proxyUserDTO, Object o, int i )
      {
         model.changeEnabled( proxyUserDTO );
         return null;
      }

      public int getColumnCount()
      {
         return 3;
      }

      public String getColumnName( int i )
      {
         return new String[]{
         text( AdministrationResources.user_enabled_label ),
         text( AdministrationResources.username_label ),
         text(AdministrationResources.description_label)}[i];
      }

      public Object getColumnValue( ProxyUserDTO proxyUserDTO, int i )
      {
         switch (i)
         {
            case 0:
               return proxyUserDTO.disabled().get();
            case 1:
               return proxyUserDTO.username().get();
            case 2:
               return proxyUserDTO.description().get();
         }
         return null;
      }
   }
}