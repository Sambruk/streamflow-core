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

package se.streamsource.streamflow.client.ui;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.application.error.ErrorResources;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.resource.user.NewProxyUserCommand;
import se.streamsource.streamflow.resource.user.NewUserCommand;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/**
 * Select a name for something.
 */
public class CreateProxyUserDialog
      extends JPanel
{
   public JTextField usernameField;
   public JTextField descriptionField;
   public JPasswordField passwordField;

   @Uses
   DialogService dialogs;

   @Structure
   ValueBuilderFactory vbf;
   private NewProxyUserCommand command;

   public CreateProxyUserDialog( @Service ApplicationContext context )
   {
      super( new BorderLayout() );

      FormLayout layout = new FormLayout( "60dlu, 5dlu, 120dlu:grow", "pref, pref, pref, pref, 10dlu, pref, pref, pref, pref" );

      JPanel form = new JPanel( layout );
      form.setFocusable( false );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout,
            form );

      usernameField = new JTextField();
      descriptionField = new JTextField();
      passwordField = new JPasswordField();

      builder.add(new JLabel( i18n.text( AdministrationResources.username_label ) ));
      builder.nextColumn(2);
      builder.add(usernameField);
      builder.nextLine();
      builder.add(new JLabel( i18n.text( AdministrationResources.description_label ) ));
      builder.nextColumn(2);
      builder.add(descriptionField);
      builder.nextLine();
      builder.add(new JLabel( i18n.text( AdministrationResources.password_label ) ));
      builder.nextColumn(2);
      builder.add(passwordField);
      builder.nextLine(2);

      setActionMap( context.getActionMap( this ) );

      add(form, BorderLayout.CENTER);
   }

   public NewProxyUserCommand userCommand()
   {
      return command;
   }

   @Action
   public void execute()
   {
      ValueBuilder<NewProxyUserCommand> builder = vbf.newValueBuilder( NewProxyUserCommand.class );
      try
      {
         builder.prototype().username().set( usernameField.getText() );
         builder.prototype().name().set( descriptionField.getText() );
         builder.prototype().password().set( String.valueOf( passwordField.getPassword() ) );
         command = builder.newInstance();
      } catch(ConstraintViolationException e)
      {
         dialogs.showOkCancelHelpDialog( WindowUtils.findWindow( this ), new JLabel( i18n.text( ErrorResources.username_password_cviolation ) ) );
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