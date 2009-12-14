/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;

/**
 * A basic dialog for resetting user passwords.
 */
public class ResetPasswordDialog
      extends JPanel
{
   public JPasswordField passwordField;
   public JPasswordField confirmPasswordField;

   String password;

   @Uses
   DialogService dialogs;

   public ResetPasswordDialog( @Service ApplicationContext context )
   {
      super( new BorderLayout() );

      setActionMap( context.getActionMap( this ) );

      JPanel passwordDialog = new JPanel( new BorderLayout() );
      passwordDialog.add( new JLabel( i18n.text( AdministrationResources.new_password ) ), BorderLayout.NORTH );
      passwordField = new JPasswordField();
      passwordDialog.add( passwordField, BorderLayout.CENTER );

      JPanel confirmPasswordDialog = new JPanel( new BorderLayout() );
      confirmPasswordDialog.add( new JLabel( i18n.text( AdministrationResources.confirm_password ) ), BorderLayout.NORTH );
      confirmPasswordField = new JPasswordField();
      confirmPasswordDialog.add( confirmPasswordField, BorderLayout.CENTER );

      add( passwordDialog, BorderLayout.CENTER );
      add( confirmPasswordDialog, BorderLayout.SOUTH );
   }

   public String password()
   {
      return password;
   }

   @org.jdesktop.application.Action
   public void execute()
   {
      if (new String( passwordField.getPassword() ).equals( new String( confirmPasswordField.getPassword() ) ))
      {
         password = new String( passwordField.getPassword() );
      } else
      {
         passwordField.setText( "" );
         confirmPasswordField.setText( "" );
         dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), new JLabel( i18n.text( AdministrationResources.passwords_dont_match ) ) );
         return;
      }
      WindowUtils.findWindow( this ).dispose();
   }

   @org.jdesktop.application.Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}
