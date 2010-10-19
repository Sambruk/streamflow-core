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

package se.streamsource.streamflow.client.ui.administration;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;

/**
 * Dialog for changing password
 */
public class ChangePasswordDialog
      extends JPanel
{
   private JPasswordField confirmPassword;
   private JPasswordField newPassword;

   private
   @Service
   DialogService dialogs;

   private ChangePasswordCommand command;

   public ChangePasswordDialog( @Service ApplicationContext context, @Structure ValueBuilderFactory vbf, @Structure ObjectBuilderFactory obf )
   {
      setActionMap( context.getActionMap( this ) );

      StateBinder binder = obf.newObject( StateBinder.class );
      binder.bindingTemplate( ChangePasswordCommand.class );

      FormLayout layout = new FormLayout(
            "75dlu, 5dlu, 120dlu", "pref, pref, pref" );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout, this );
//      builder.setDefaultDialogBorder();

      StateBinder passwordBinder = obf.newObject( StateBinder.class );
      passwordBinder.setResourceMap( context.getResourceMap( getClass() ) );

      command = vbf.newValue( ChangePasswordCommand.class ).<ChangePasswordCommand>buildWith().prototype();
      
      JLabel confirmPasswordLabel;
      builder.add(new JLabel( i18n.text( AdministrationResources.old_password ) ));
      builder.nextColumn(2);
      builder.add( passwordBinder.bind(PASSWORD.newField(), command.oldPassword()));
      builder.nextLine();
      builder.add(new JLabel( i18n.text( AdministrationResources.new_password ) ));
      builder.nextColumn(2);
      builder.add( passwordBinder.bind(newPassword = (JPasswordField) PASSWORD.newField(), command.newPassword()));
      builder.nextLine();
      builder.add(confirmPasswordLabel = new JLabel( i18n.text( AdministrationResources.confirm_password ) ));
      builder.nextColumn(2);
      builder.add(confirmPassword = (JPasswordField) PASSWORD.newField());
      builder.nextLine();
      confirmPasswordLabel.setLabelFor(confirmPassword);
      
//      BindingFormBuilder bb = new BindingFormBuilder( builder, passwordBinder );
//      bb.appendLine( AdministrationResources.old_password, BindingFormBuilder.Fields.PASSWORD, command.oldPassword() );
//      bb.appendLine( AdministrationResources.new_password, newPassword = (JPasswordField) BindingFormBuilder.Fields.PASSWORD.newField(), command.newPassword() );
//      JLabel label = builder.append( bb.getResource( AdministrationResources.confirm_password ) );
//      builder.nextLine();
//      builder.append( confirmPassword = new JPasswordField() );
//      label.setLabelFor( confirmPassword );
//      builder.nextLine();
   }

   @Action
   public void execute()
   {
      if (new String( confirmPassword.getPassword() ).equals( new String( newPassword.getPassword() ) ))
      {
         WindowUtils.findWindow( this ).dispose();
      } else
      {
         dialogs.showOkDialog( this, new JLabel( i18n.text( AdministrationResources.passwords_do_not_match ) ) );
      }
   }

   @Action
   public void close()
   {
      command = null;
      WindowUtils.findWindow( this ).dispose();
   }

   public ChangePasswordCommand command()
   {
      return command;
   }
}
