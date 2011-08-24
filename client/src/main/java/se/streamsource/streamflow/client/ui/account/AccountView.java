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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.api.administration.ChangePasswordDTO;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.PASSWORD;
import static se.streamsource.streamflow.client.util.BindingFormBuilder.Fields.TEXTFIELD;

/**
 * JAVADOC
 */
public class AccountView extends JScrollPane
{

   @Structure
   private Module module;

   @Uses
   private AccountModel model;

   @Service
   private DialogService dialogs;

   private ValueBuilder<AccountSettingsValue> accountSettingsBuilder;
   private StateBinder accountBinder;
   private StateBinder connectedBinder;
   private ApplicationContext context;

   public FormEditor accountEditor;
   public JPanel accountForm;
   public JPanel contactForm;

   public AccountView(@Service ApplicationContext context, @Structure Module module )
   {
      this.context = context;
      ActionMap am = context.getActionMap(this);
      setActionMap(am);

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

      accountForm = new JPanel();
      panel.add(accountForm, BorderLayout.NORTH);
      FormLayout accountLayout = new FormLayout("75dlu, 5dlu, 120dlu:grow",
            "pref, pref, pref, pref, pref");

      DefaultFormBuilder accountBuilder = new DefaultFormBuilder(accountLayout,
            accountForm);
      // accountBuilder.setDefaultDialogBorder();

      accountBinder = module.objectBuilderFactory().newObject(StateBinder.class);
      accountBinder.setResourceMap(context.getResourceMap(getClass()));
      connectedBinder = module.objectBuilderFactory().newObject(StateBinder.class);
      AccountSettingsValue accountTemplate = accountBinder
            .bindingTemplate(AccountSettingsValue.class);

      accountBuilder.appendSeparator(i18n
            .text(AccountResources.account_separator));
      accountBuilder.nextLine();

      accountBuilder.add(new JLabel(i18n
            .text(AccountResources.account_name_label)));
      accountBuilder.nextColumn(2);
      accountBuilder.add(accountBinder.bind(TEXTFIELD.newField(),
            accountTemplate.name()));
      accountBuilder.nextLine();

      accountBuilder.add(new JLabel(i18n.text(AccountResources.server_label)));
      accountBuilder.nextColumn(2);
      accountBuilder.add(accountBinder.bind(TEXTFIELD.newField(),
            accountTemplate.server()));
      accountBuilder.nextLine();

      accountBuilder
            .add(new JLabel(i18n.text(AccountResources.username_label)));
      accountBuilder.nextColumn(2);
      accountBuilder.add(accountBinder.bind(TEXTFIELD.newField(),
            accountTemplate.userName()));
      accountBuilder.nextLine();

      accountBuilder
            .add(new JLabel(i18n.text(AccountResources.password_label)));
      accountBuilder.nextColumn(2);
      accountBuilder.add(accountBinder.bind(PASSWORD.newField(),
            accountTemplate.password()));
      accountBuilder.nextLine();

      accountEditor = new FormEditor(accountForm);

      contactForm = new JPanel();
      panel.add(contactForm, BorderLayout.CENTER);
      FormLayout contactLayout = new FormLayout("75dlu, 5dlu, 120dlu:grow",
            "pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref");

      DefaultFormBuilder contactBuilder = new DefaultFormBuilder(contactLayout,
            contactForm);
      // contactBuilder.setDefaultDialogBorder();

      contactBuilder.nextColumn(2);
      contactBuilder.add(new JToggleButton(am.get("edit")));
      contactBuilder.nextLine();
      contactBuilder.nextColumn(2);
      contactBuilder.add(new JButton(am.get("test")));
      contactBuilder.nextLine(2);

      contactBuilder.nextColumn(2);
      contactBuilder.add(new JButton(am.get("changePassword")));

      setViewportView(panel);
   }

   @Action(block = Task.BlockingScope.APPLICATION)
   public Task test()
   {
      Task<String, Void> task = module.objectBuilderFactory().newObjectBuilder(TestConnectionTask.class).use(model).newInstance();

      task.addTaskListener(new TaskListener.Adapter<String, Void>()
      {
         @Override
         public void succeeded(TaskEvent<String> stringTaskEvent)
         {
            String result = stringTaskEvent.getValue();
            dialogs.showMessageDialog( AccountView.this, result, "Server Version:" );
         }

         @Override
         public void failed(TaskEvent<Throwable> throwableTaskEvent)
         {
            try
            {
               throw throwableTaskEvent.getValue();
            } catch (ResourceException e)
            {
               if (e.getStatus().equals( Status.CLIENT_ERROR_UNAUTHORIZED ))
               {
                  dialogs.showMessageDialog(AccountView.this, i18n.text(AccountResources.wrong_user_password), "Info");
               } else
               {
                  dialogs.showMessageDialog(AccountView.this, i18n.text(AccountResources.connection_not_ok)+ " " + e.getStatus().toString(), "Info");
               }
            } catch (Throwable throwable)
            {
               throwable.printStackTrace();
            }

         }
      });

      return task;
   }

   @Action
   public void changePassword() throws Exception
   {
      ChangePasswordDialog changePasswordDialog = module.objectBuilderFactory().newObject(ChangePasswordDialog.class);
      dialogs.showOkCancelHelpDialog(this, changePasswordDialog, i18n
            .text(WorkspaceResources.change_password_title));

      ChangePasswordDTO DTO = changePasswordDialog.command();
      if (DTO != null)
      {
         if (!DTO.oldPassword().get().equals(
               model.settings().password().get()))
         {
            dialogs.showOkDialog(this, new JLabel(i18n
                  .text( AdministrationResources.old_password_incorrect)));
         } else
         {
            model.changePassword(DTO.oldPassword().get(), DTO.newPassword().get());
         }
      }
   }

   @Action
   public void edit() throws UnitOfWorkCompletionException
   {
      if (!accountEditor.isEditing())
      {
         accountEditor.edit();
      } else
      {
         accountEditor.view();

         // Update settings
         model.updateSettings(accountSettingsBuilder.newInstance());
      }
   }

   @Override
   public void addNotify()
   {
      super.addNotify();

      accountSettingsBuilder = model.settings().buildWith();
      accountBinder.updateWith(accountSettingsBuilder.prototype());
      connectedBinder.update();
   }

   public void setModel( AccountModel model)
   {
      this.model = model;
   }
}
