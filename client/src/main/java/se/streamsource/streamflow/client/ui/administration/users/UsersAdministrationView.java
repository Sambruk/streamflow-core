/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.administration.users;

import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.FileNameExtensionFilter;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.CreateUserDialog;
import se.streamsource.streamflow.client.ui.ResetPasswordDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

public class UsersAdministrationView
      extends JPanel
{
   private UsersAdministrationModel model;

   @Uses
   Iterable<CreateUserDialog> userDialogs;

   @Uses
   Iterable<ResetPasswordDialog> resetPwdDialogs;

   @Service
   DialogService dialogs;

   JXTable usersTable;

   public UsersAdministrationView( @Service ApplicationContext context, @Uses UsersAdministrationModel model )
   {
      ApplicationActionMap am = context.getActionMap( this );
      setActionMap( am );

      this.model = model;
      usersTable = new JXTable( model );
      //usersTable.getColumn( 0 ).setCellRenderer( new DefaultTableRenderer( new CheckBoxProvider() ) );
      //usersTable.getColumn( 0 ).setMaxWidth( 30 );
      //usersTable.getColumn( 0 ).setResizable( false );
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
   }


   @org.jdesktop.application.Action
   public void createUser()
   {
      CreateUserDialog dialog = userDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.create_user_title ) );

      if (dialog.username() != null && dialog.password() != null)
      {
         model.createUser( dialog.username(), dialog.password() );
      }
   }

   @org.jdesktop.application.Action
   public void importUserList()
   {

      // Ask the user for a file to import user/pwd pairs from
      // Can be either Excels or CVS format
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
      fileChooser.setMultiSelectionEnabled( false );
      fileChooser.addChoosableFileFilter( new FileNameExtensionFilter(
            text( AdministrationResources.import_files ), true, "xls", "csv", "txt" ) );
      fileChooser.setDialogTitle( text( AdministrationResources.import_users ) );
      int returnVal = fileChooser.showOpenDialog( (UsersAdministrationView.this) );
      if (returnVal != JFileChooser.APPROVE_OPTION)
      {
         return;
      }

      model.importUsers( fileChooser.getSelectedFile().getAbsoluteFile() );

   }

   @org.jdesktop.application.Action
   public void resetPassword()
   {
      ResetPasswordDialog dialog = resetPwdDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.reset_password_title ) + ": " + model.getValueAt( usersTable.getSelectedRow(), 1 ) );

      if (dialog.password() != null)
      {
         model.resetPassword( usersTable.getSelectedRow(), dialog.password() );
      }
   }

}
