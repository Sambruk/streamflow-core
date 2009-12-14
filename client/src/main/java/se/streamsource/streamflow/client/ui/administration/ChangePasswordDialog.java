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

package se.streamsource.streamflow.client.ui.administration;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * Dialog for changing password
 */
public class ChangePasswordDialog
      extends JPanel
{
   private StateBinder passwordBinder;
   private JPasswordField confirmPassword;
   private JPasswordField newPassword;

   private
   @Service
   DialogService dialogs;

   private ChangePasswordCommand command;

   public ChangePasswordDialog( @Service ApplicationContext context, @Structure ValueBuilderFactory vbf )
   {
      setActionMap( context.getActionMap( this ) );

      StateBinder binder = new StateBinder();
      binder.bindingTemplate( ChangePasswordCommand.class );

      FormLayout layout = new FormLayout(
            "200dlu",
//                "right:max(40dlu;p), 4dlu, 80dlu, 7dlu, " // 1st major column
//                        + "right:max(40dlu;p), 4dlu, 80dlu",        // 2nd major column
            "" );                                      // add rows dynamically
      DefaultFormBuilder builder = new DefaultFormBuilder( layout, this );
      builder.setDefaultDialogBorder();

      passwordBinder = new StateBinder();
      passwordBinder.setResourceMap( context.getResourceMap( getClass() ) );

      command = vbf.newValue( ChangePasswordCommand.class ).<ChangePasswordCommand>buildWith().prototype();

      BindingFormBuilder bb = new BindingFormBuilder( builder, passwordBinder );

      bb.appendLine( AdministrationResources.old_password, BindingFormBuilder.Fields.PASSWORD, command.oldPassword() );
      bb.appendLine( AdministrationResources.new_password, newPassword = (JPasswordField) BindingFormBuilder.Fields.PASSWORD.newField(), command.newPassword() );
      JLabel label = builder.append( bb.getResource( AdministrationResources.confirm_password ) );
      builder.nextLine();
      builder.append( confirmPassword = new JPasswordField() );
      label.setLabelFor( confirmPassword );
      builder.nextLine();
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
