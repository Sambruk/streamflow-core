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

import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.PASSWORD;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.TEXTFIELD;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;

import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.ConnectionException;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.FormEditor;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.task.TaskResources;
import se.streamsource.streamflow.client.ui.workspace.TestConnectionTask;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.ContactPhoneValue;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * JAVADOC
 */
public class AccountView extends JScrollPane implements Observer
{

   @Structure
   private ValueBuilderFactory vbf;

   @Structure
   private UnitOfWorkFactory uowf;

   @Uses
   private AccountModel model;

   @Uses
   private ObjectBuilder<TestConnectionTask> testConnectionTasks;

   @Service
   private DialogService dialogs;

   @Uses
   Iterable<ChangePasswordDialog> changePasswords;

   private ValueBuilder<AccountSettingsValue> accountSettingsBuilder;
   private StateBinder accountBinder;
   private StateBinder connectedBinder;
   private StateBinder contactBinder;
   private StateBinder phoneNumberBinder;
   private StateBinder emailBinder;
   // private StateBinder addressBinder;

   public FormEditor accountEditor;
   public JPanel accountForm;
   public JPanel contactForm;

   public AccountView(@Service ApplicationContext context)
   {
      ApplicationActionMap am = context.getActionMap(this);
      setActionMap(am);

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

      accountForm = new JPanel();
      panel.add(accountForm, BorderLayout.NORTH);
      FormLayout accountLayout = new FormLayout("75dlu, 5dlu, 120dlu:grow",
            "pref, pref, pref, pref, pref");

      DefaultFormBuilder accountBuilder = new DefaultFormBuilder(accountLayout,
            accountForm);
//      accountBuilder.setDefaultDialogBorder();

      accountBinder = new StateBinder();
      accountBinder.setResourceMap(context.getResourceMap(getClass()));
      connectedBinder = new StateBinder();
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
            "pref, pref, pref, pref, pref, pref, pref");

      contactBinder = new StateBinder();
      contactBinder.setResourceMap(context.getResourceMap(getClass()));
      ContactValue contactTemplate = contactBinder
            .bindingTemplate(ContactValue.class);

      phoneNumberBinder = new StateBinder();
      phoneNumberBinder.setResourceMap(context.getResourceMap(getClass()));
      ContactPhoneValue phoneTemplate = phoneNumberBinder
            .bindingTemplate(ContactPhoneValue.class);

      emailBinder = new StateBinder();
      emailBinder.setResourceMap(context.getResourceMap(getClass()));
      ContactEmailValue emailTemplate = emailBinder
            .bindingTemplate(ContactEmailValue.class);

      DefaultFormBuilder contactBuilder = new DefaultFormBuilder(contactLayout,
            contactForm);
//      contactBuilder.setDefaultDialogBorder();

      contactBuilder.nextColumn(2);
      contactBuilder.add(new JToggleButton(am.get("edit")));
      contactBuilder.nextLine();
      contactBuilder.nextColumn(2);
      contactBuilder.add(new JButton(am.get("test")));
      contactBuilder.nextLine();

      contactBuilder.appendSeparator(i18n
            .text(AccountResources.contact_info_for_user_separator));
//      contactBuilder.appendSeparator(i18n
//            .text(AccountResources.contact_info_separator));
      contactBuilder.nextLine();

      contactBuilder.add(new JLabel(i18n.text(WorkspaceResources.name_label)));
      contactBuilder.nextColumn(2);
      contactBuilder.add(contactBinder.bind(TEXTFIELD.newField(),
            contactTemplate.name()));
      contactBuilder.nextLine();

      contactBuilder.add(new JLabel(i18n.text(WorkspaceResources.email_label)));
      contactBuilder.nextColumn(2);
      contactBuilder.add(emailBinder.bind(TEXTFIELD.newField(), emailTemplate
            .emailAddress()));
      contactBuilder.nextLine();

      contactBuilder.add(new JLabel(i18n.text(WorkspaceResources.phone_label)));
      contactBuilder.nextColumn(2);
      contactBuilder.add(phoneNumberBinder.bind(TEXTFIELD.newField(),
            phoneTemplate.phoneNumber()));
      contactBuilder.nextLine();

      contactBuilder.nextColumn(2);
      contactBuilder.add(new JButton(am.get("changePassword")));

      contactBinder.addObserver( this );
      phoneNumberBinder.addObserver( this );
      emailBinder.addObserver( this );
      setViewportView(panel);
   }

   @Action(block = Task.BlockingScope.APPLICATION)
   public Task test()
   {
      Task<String, Void> task = testConnectionTasks.use(model).newInstance();

      task.addTaskListener(new TaskListener.Adapter<String, Void>()
      {
         @Override
         public void succeeded(TaskEvent<String> stringTaskEvent)
         {
            String result = stringTaskEvent.getValue();
            dialogs.showOkDialog(AccountView.this, new JLabel(result));
         }

         @Override
         public void failed(TaskEvent<Throwable> throwableTaskEvent)
         {
            try
            {
               throw throwableTaskEvent.getValue();
            } catch (ConnectionException e)
            {
               dialogs.showOkDialog(AccountView.this, new JLabel(
                     "#Connection is not ok:" + e.status().getName()));
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
      ChangePasswordDialog changePasswordDialog = changePasswords.iterator()
            .next();
      dialogs.showOkCancelHelpDialog(this, changePasswordDialog, i18n
            .text(WorkspaceResources.change_password_title));

      ChangePasswordCommand command = changePasswordDialog.command();
      if (command != null)
      {
         if (!command.oldPassword().get().equals(
               model.settings().password().get()))
         {
            dialogs.showOkDialog(this, new JLabel(i18n
                  .text(AdministrationResources.old_password_incorrect)));
         } else
         {
            model.changePassword(command);
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
         contactBinder.updateWith(model.getContact());
         phoneNumberBinder.updateWith(model.getPhoneNumber());
         emailBinder.updateWith(model.getEmailAddress());
      }
   }

   @Override
   public void addNotify()
   {
      super.addNotify();

      accountSettingsBuilder = model.settings().buildWith();
      accountBinder.updateWith(accountSettingsBuilder.prototype());
      contactBinder.updateWith(model.getContact());
      phoneNumberBinder.updateWith(model.getPhoneNumber());
      emailBinder.updateWith(model.getEmailAddress());
      connectedBinder.update();
   }

   public void update(Observable observable, Object arg)
   {
      Property property = (Property) arg;
      if (property.qualifiedName().name().equals("name"))
      {
         try
         {
            model.changeName((String) property.get());
         } catch (ResourceException e)
         {
            throw new OperationException(TaskResources.could_not_change_name, e);
         }
      } else if (property.qualifiedName().name().equals("phoneNumber"))
      {
         try
         {
            model.changePhoneNumber((String) property.get());
         } catch (ResourceException e)
         {
            throw new OperationException(
                  TaskResources.could_not_change_phone_number, e);
         }
      } else if (property.qualifiedName().name().equals("emailAddress"))
      {
         try
         {
            model.changeEmailAddress((String) property.get());
         } catch (ResourceException e)
         {
            throw new OperationException(
                  TaskResources.could_not_change_email_address, e);
         }
      }
   }

}
