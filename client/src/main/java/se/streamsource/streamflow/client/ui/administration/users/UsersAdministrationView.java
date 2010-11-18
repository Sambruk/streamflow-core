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

package se.streamsource.streamflow.client.ui.administration.users;

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;
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
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.FileNameExtensionFilter;
import se.streamsource.streamflow.client.util.RefreshWhenVisible;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.resource.user.UserEntityValue;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;

import static se.streamsource.streamflow.client.util.i18n.text;

public class UsersAdministrationView
      extends JPanel
   implements TransactionListener
{
   private UsersAdministrationModel model;

   @Uses
   Iterable<CreateUserDialog> userDialogs;

   @Uses
   Iterable<ResetPasswordDialog> resetPwdDialogs;

   @Service
   DialogService dialogs;

   JXTable usersTable;
   private EventJXTableModel<UserEntityValue> tableModel;

   public UsersAdministrationView( @Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf)
   {
      ApplicationActionMap am = context.getActionMap( this );
      setActionMap( am );

      this.model = obf.newObjectBuilder( UsersAdministrationModel.class ).use( client ).newInstance();

      TableFormat<UserEntityValue> userAdminTableFormat = new UserAdminTableFormat();

      tableModel = new EventJXTableModel<UserEntityValue>( model.getEventList(), userAdminTableFormat );

      usersTable = new JXTable( tableModel );
      usersTable.getColumn( 0 ).setCellRenderer( new DefaultTableRenderer( new CheckBoxProvider() ) );
      usersTable.getColumn( 0 ).setMaxWidth( 50 );
      usersTable.getColumn( 0 ).setResizable( false );
      usersTable.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "resetPassword" ) ) );

      JScrollPane scroll = new JScrollPane();
      scroll.setViewportView( usersTable );
      
      super.setLayout(new BorderLayout());
      super.add( scroll, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "createUser" ) ) );
      toolbar.add( new JButton( am.get( "resetPassword" ) ) );
      toolbar.add( new JButton( am.get( "importUserList" ) ) );
      super.add( toolbar, BorderLayout.SOUTH );

      new RefreshWhenVisible(this, model);
   }


   @org.jdesktop.application.Action
   public Task createUser()
   {
      final CreateUserDialog dialog = userDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.create_user_title ) );

      if ( dialog.userCommand() != null )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.createUser( dialog.userCommand() );
            }
         };
      } else
      {
         return null;
      }
   }

   @org.jdesktop.application.Action
   public Task importUserList()
   {

      // Ask the user for a file to import user/pwd pairs from
      // Can be either Excels or CVS format
      final JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
      fileChooser.setMultiSelectionEnabled( false );
      fileChooser.addChoosableFileFilter( new FileNameExtensionFilter(
            text( AdministrationResources.import_files ), true, "xls", "csv", "txt" ) );
      fileChooser.setDialogTitle( text( AdministrationResources.import_users ) );
      int returnVal = fileChooser.showOpenDialog( (UsersAdministrationView.this) );
      if (returnVal != JFileChooser.APPROVE_OPTION)
      {
         return null;
      }

      return new CommandTask()
      {
         @Override
         public void command()
            throws Exception
         {
            model.importUsers( fileChooser.getSelectedFile().getAbsoluteFile() );
         }
      };
   }

   @org.jdesktop.application.Action
   public Task resetPassword()
   {
      final ResetPasswordDialog dialog = resetPwdDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.reset_password_title ) + ": " + tableModel.getElementAt( usersTable.getSelectedRow()).text().get() );

      if (dialog.password() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.resetPassword( tableModel.getElementAt( usersTable.getSelectedRow()), dialog.password() );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (!Events.matches( Events.withNames("changedEnabled" ), transactions))
         model.refresh();
   }

   private class UserAdminTableFormat
      implements AdvancedTableFormat<UserEntityValue>, WritableTableFormat<UserEntityValue>
   {
      public boolean isEditable( UserEntityValue userEntityDTO, int i )
      {
         return i == 0;
      }

      public UserEntityValue setColumnValue( final UserEntityValue userEntityDTO, Object o, int i )
      {
         if (i == 0)
         {
            new CommandTask()
            {
               @Override
               protected void command() throws Exception
               {
                  model.changeDisabled( userEntityDTO );
               }
            }.execute();
         }

         ValueBuilder<UserEntityValue> builder = userEntityDTO.buildWith();
         builder.prototype().disabled().set(!builder.prototype().disabled().get());

         return builder.newInstance();
      }

      public int getColumnCount()
      {
         return 2;
      }

      public String getColumnName( int i )
      {
         return new String[]{text( AdministrationResources.user_enabled_label ), text( AdministrationResources.username_label )}[i];
      }

      public Object getColumnValue( UserEntityValue userEntityDTO, int i )
      {
         switch (i)
         {
            case 0:
               return !userEntityDTO.disabled().get();
            case 1:
               return userEntityDTO.text().get();
         }
         return null;
      }

      public Class getColumnClass( int column )
      {
         return column == 0 ? Boolean.class : Object.class;
      }

      public Comparator getColumnComparator( int column )
      {
         return null;
      }
   }
}
