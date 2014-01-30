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
package se.streamsource.streamflow.client.ui.administration.users;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.api.administration.NewUserDTO;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.util.dialog.DialogService;

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

   @Uses
   DialogService dialogs;

   @Structure
   Module module;
   private NewUserDTO DTO;

   public CreateUserDialog( @Service ApplicationContext context )
   {
      super( new BorderLayout() );

      setActionMap( context.getActionMap( this ) );
      getActionMap().put( JXDialog.CLOSE_ACTION_COMMAND, getActionMap().get("cancel" ));

      FormLayout layout = new FormLayout( "60dlu, 5dlu, 120dlu:grow", "pref, pref, pref, pref, 10dlu, pref, pref, pref, pref" );

      JPanel form = new JPanel( layout );
      form.setFocusable( false );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout,
            form );

      usernameField = new JTextField();
      passwordField = new JPasswordField();
      confirmPasswordField = new JPasswordField();

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

      add(form, BorderLayout.CENTER);
   }

   public NewUserDTO userCommand()
   {
      return DTO;
   }

   @Action
   public void execute()
   {
      if (!String.valueOf( passwordField.getPassword() ).equals( String.valueOf( confirmPasswordField.getPassword() ) ))
      {
         passwordField.setText( "" );
         confirmPasswordField.setText( "" );
         dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), new JLabel( i18n.text( AdministrationResources.passwords_dont_match ) ) );
         return;
      }

      ValueBuilder<NewUserDTO> builder = module.valueBuilderFactory().newValueBuilder(NewUserDTO.class);
      try
      {
         builder.prototype().username().set( usernameField.getText() );
         builder.prototype().password().set( String.valueOf( passwordField.getPassword() ) );
         DTO = builder.newInstance();
      } catch(ConstraintViolationException e)
      {
         dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), new JLabel( i18n.text( ErrorResources.username_password_violation ) ) );
         return;
      }
      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void cancel()
   {
      WindowUtils.findWindow( this ).dispose();
   }
}