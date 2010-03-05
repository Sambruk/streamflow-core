/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Select a name for something.
 */
public class CreateUserDialog
      extends JPanel
{
   public JTextField usernameField;
   public JPasswordField passwordField;
   public JPasswordField confirmPasswordField;
   public JTextField nameField;
   public JTextField phoneField;
   public JTextField emailField;
   
   String username;
   String password;
   String name;
   String phone;
   String email;

   @Uses
   DialogService dialogs;

   public CreateUserDialog( @Service ApplicationContext context )
   {
      super( new BorderLayout() );
      
      FormLayout layout = new FormLayout( "60dlu, 5dlu, 120dlu:grow", "pref, pref, pref, pref, 10dlu, pref, pref, pref, pref" );

      JPanel form = new JPanel( layout );
      form.setFocusable( false );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout,
            form );

      nameField = new JTextField();
      emailField = new JTextField();
      phoneField = new JTextField();
      usernameField = new JTextField();
      passwordField = new JPasswordField();
      confirmPasswordField = new JPasswordField();
      
//      builder.appendSeparator(i18n.text(StreamFlowResources.user_info_separator));
//      builder.nextLine();
      builder.add(new JLabel( i18n.text( AdministrationResources.username_label ) ));
      builder.nextColumn(2);
      builder.add(usernameField);
      builder.nextLine();
      builder.add(new JLabel( i18n.text( AdministrationResources.password_label ) ));
      builder.nextColumn(2);
      builder.add(passwordField);
      builder.nextLine();
      builder.add(new JLabel( i18n.text( AdministrationResources.confirm_password_label ) ));
      builder.nextColumn(2);
      builder.add(confirmPasswordField);
      builder.nextLine(2);
      
//      builder.appendSeparator(i18n.text(StreamFlowResources.contact_info_separator));
//      builder.nextLine();
//      builder.add(new JLabel( i18n.text( AdministrationResources.name_label ) ));
//      builder.nextColumn(2);
//      builder.add(nameField);
//      builder.nextLine();
//      builder.add(new JLabel( i18n.text( AdministrationResources.email_label ) ));
//      builder.nextColumn(2);
//      builder.add(emailField);
//      builder.nextLine();
//      builder.add(new JLabel( i18n.text( AdministrationResources.phone_label ) ));
//      builder.nextColumn(2);
//      builder.add(phoneField);
//      builder.nextLine();

      setActionMap( context.getActionMap( this ) );

      add(form, BorderLayout.CENTER);
   }

   public String username()
   {
      return username;
   }

   public String password()
   {
      return password;
   }

   @Action
   public void execute()
   {
      if ( String.valueOf( passwordField.getPassword() ).equals( String.valueOf( confirmPasswordField.getPassword() ) ))
      {
         username = usernameField.getText();
         password = String.valueOf( passwordField.getPassword() );
      } else
      {
         passwordField.setText( "" );
         confirmPasswordField.setText( "" );
         dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), new JLabel( i18n.text( AdministrationResources.passwords_dont_match ) ) );
         return;
      }
      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}