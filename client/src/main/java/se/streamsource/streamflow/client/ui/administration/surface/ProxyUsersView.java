/**
 *
 * Copyright 2009-2013 Jayway Products AB
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

import static se.streamsource.streamflow.client.util.i18n.text;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.matches;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withNames;

import java.awt.BorderLayout;
import java.util.Comparator;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

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
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.api.administration.ProxyUserDTO;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.users.ResetPasswordDialog;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;

public class ProxyUsersView
      extends JPanel
   implements TransactionListener
{
   private ProxyUsersModel model;

   @Structure
   Module module;

   @Service
   DialogService dialogs;

   JXTable proxyUsersTable;
   private EventJXTableModel<ProxyUserDTO> tableModel;

   public ProxyUsersView( @Service ApplicationContext context, @Uses ProxyUsersModel model)
   {
      ApplicationActionMap am = context.getActionMap( this );
      setActionMap( am );

      this.model = model;
      TableFormat<ProxyUserDTO> tableFormat = new ProxyUsersTableFormat();

      tableModel = new EventJXTableModel<ProxyUserDTO>( model.getEventList(), tableFormat );

      proxyUsersTable = new JXTable( tableModel );
      proxyUsersTable.getColumn( 0 ).setCellRenderer(new DefaultTableRenderer(new CheckBoxProvider()));
      proxyUsersTable.getColumn( 0 ).setMaxWidth(60);
      proxyUsersTable.getColumn( 0 ).setResizable(false);
      proxyUsersTable.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("resetPassword")));

      JScrollPane scroll = new JScrollPane();
      scroll.setViewportView( proxyUsersTable );

      super.setLayout(new BorderLayout());
      super.add( scroll, BorderLayout.CENTER );

      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "resetPassword" ) );
      options.add( am.get( "remove" ) );

      JPanel toolbar = new JPanel();
      toolbar.add( new StreamflowButton( am.get( "add" ) ) );
      toolbar.add( new StreamflowButton( new OptionsAction( options ) ) );
      super.add( toolbar, BorderLayout.SOUTH );

      proxyUsersTable.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "resetPassword" ), am.get( "remove") ) );
      new RefreshWhenShowing(this, model);
   }


   @org.jdesktop.application.Action
   public Task add()
   {
      final CreateProxyUserDialog dialog = module.objectBuilderFactory().newObject(CreateProxyUserDialog.class);
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
      final ResetPasswordDialog dialog = module.objectBuilderFactory().newObject(ResetPasswordDialog.class);

      final ProxyUserDTO proxyUser = tableModel.getElementAt( proxyUsersTable.convertRowIndexToModel( proxyUsersTable.getSelectedRow()) );

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
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      final ProxyUserDTO proxyUser = tableModel.getElementAt( proxyUsersTable.convertRowIndexToView( proxyUsersTable.getSelectedRow()) );
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

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withNames("createdProxyUser", "changedEnabled" ), transactions ))
      {
         model.refresh();
      }
   }

   private class ProxyUsersTableFormat
      implements WritableTableFormat<ProxyUserDTO>, AdvancedTableFormat<ProxyUserDTO>
   {
      public boolean isEditable( ProxyUserDTO proxyUserDTO, int i )
      {
         return i == 0;
      }

      public ProxyUserDTO setColumnValue( final ProxyUserDTO proxyUserDTO, final Object o, int i )
      {
         new CommandTask()
         {
            @Override
            protected void command() throws Exception
            {
               model.changeEnabled( proxyUserDTO, (Boolean)o );
            }
         }.execute();
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

      public Class getColumnClass(int i)
      {
         return new Class[]{Boolean.class, String.class, String.class}[i];
      }

      public Comparator getColumnComparator(int i)
      {
         return null;
      }

      public Object getColumnValue( ProxyUserDTO proxyUserDTO, int i )
      {
         switch (i)
         {
            case 0:
               return !proxyUserDTO.disabled().get();
            case 1:
               return proxyUserDTO.username().get();
            case 2:
               return proxyUserDTO.description().get();
         }
         return null;
      }
   }
}