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

package se.streamsource.streamflow.client.ui.account;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.api.administration.ChangePasswordDTO;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.*;

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.*;

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

   private ChangePasswordDTO DTO;

   public ChangePasswordDialog( @Service ApplicationContext context, @Structure ValueBuilderFactory vbf, @Structure ObjectBuilderFactory obf )
   {
      setActionMap( context.getActionMap( this ) );

      StateBinder binder = obf.newObject( StateBinder.class );
      binder.bindingTemplate( ChangePasswordDTO.class );

      FormLayout layout = new FormLayout(
            "75dlu, 5dlu, 120dlu", "pref, pref, pref" );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout, this );
//      builder.setDefaultDialogBorder();

      StateBinder passwordBinder = obf.newObject( StateBinder.class );
      passwordBinder.setResourceMap( context.getResourceMap( getClass() ) );

      DTO = vbf.newValue( ChangePasswordDTO.class ).<ChangePasswordDTO>buildWith().prototype();
      
      JLabel confirmPasswordLabel;
      builder.add(new JLabel( i18n.text( AdministrationResources.old_password ) ));
      builder.nextColumn(2);
      builder.add( passwordBinder.bind(PASSWORD.newField(), DTO.oldPassword()));
      builder.nextLine();
      builder.add(new JLabel( i18n.text( AdministrationResources.new_password ) ));
      builder.nextColumn(2);
      builder.add( passwordBinder.bind(newPassword = (JPasswordField) PASSWORD.newField(), DTO.newPassword()));
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
      DTO = null;
      WindowUtils.findWindow( this ).dispose();
   }

   public ChangePasswordDTO command()
   {
      return DTO;
   }
}
