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
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.ConnectionException;
import se.streamsource.streamflow.client.domain.individual.RegistrationException;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.*;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.FormEditor;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import static se.streamsource.streamflow.client.ui.administration.AccountResources.*;
import se.streamsource.streamflow.client.ui.workspace.TestConnectionTask;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class AccountView
        extends JScrollPane
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

    private ValueBuilder<AccountSettingsValue> builder;
    private StateBinder settingsBinder;
    private StateBinder connectedBinder;
    public FormEditor editor;
    public JPanel settingsForm;

    public AccountView(@Service ApplicationContext context)
    {
        ApplicationActionMap am = context.getActionMap(this);
        setActionMap(am);

        JPanel panel = new JPanel(new BorderLayout());

        settingsForm = new JPanel();
        panel.add(settingsForm, BorderLayout.NORTH);
        FormLayout layout = new FormLayout(
                "200dlu",
//                "right:max(40dlu;p), 4dlu, 80dlu, 7dlu, " // 1st major column
//                        + "right:max(40dlu;p), 4dlu, 80dlu",        // 2nd major column
                "");                                      // add rows dynamically
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, settingsForm);
        builder.setDefaultDialogBorder();

        settingsBinder = new StateBinder();
        settingsBinder.setResourceMap(context.getResourceMap(getClass()));
        connectedBinder = new StateBinder();
        AccountSettingsValue template = settingsBinder.bindingTemplate(AccountSettingsValue.class);

        BindingFormBuilder bb = new BindingFormBuilder(builder, settingsBinder);
        bb.appendSeparator(account_separator)
                .appendLine(name_label, TEXTFIELD, template.name())
                .appendLine(server_label, TEXTFIELD, template.server())
                .appendLine(username_label, TEXTFIELD, template.userName())
                .appendLine(password_label, PASSWORD, template.password());

        editor = new FormEditor(settingsForm);

        JPanel otherForm = new JPanel();
        panel.add(otherForm, BorderLayout.CENTER);
        layout = new FormLayout(
                "200dlu",
//                "right:max(40dlu;p), 4dlu, 80dlu, 7dlu, " // 1st major column
//                        + "right:max(40dlu;p), 4dlu, 80dlu",        // 2nd major column
                "");                                      // add rows dynamically
        builder = new DefaultFormBuilder(layout, otherForm);
        builder.setDefaultDialogBorder();
        bb = new BindingFormBuilder(builder, settingsBinder);
        bb
                .appendToggleButtonLine(am.get("edit"))
                .appendSeparator(account_separator)
                .appendSeparator(testconnection_separator)
                .appendButtonLine(am.get("test"))

                .appendSeparator(registration_separator)
                .appendButtonLine(am.get("register"))

                .appendSeparator(connected_separator);

/*
        bb = new BindingFormBuilder(builder, connectedBinder);
        bb.appendLine(connected_separator, CHECKBOX, new AbstractPropertyInstance<Boolean>(new GenericPropertyInfo(new MetaInfo(), false, false, QualifiedName.fromClass(AccountConnection.class, "connected"), Boolean.class))
        {
            public Boolean get()
            {
                return model.account().isEnabled();
            }

            public void set(Boolean newValue) throws IllegalArgumentException, IllegalStateException
            {
                if (newValue)
                    try
                    {
                        model.account().server();
                    } catch (OperationException e)
                    {
                        throw new IllegalArgumentException(e.getMessage());
                    }
                else
                    model.account().disconnect();
            }
        });
*/

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
                dialogs.showOkDialog(AccountView.this, new JLabel("<html>#Connection is ok:<br/>" + stringTaskEvent.getValue() + "</html>"));
            }

            @Override
            public void failed(TaskEvent<Throwable> throwableTaskEvent)
            {
                try
                {
                    throw throwableTaskEvent.getValue();
                } catch (ConnectionException e)
                {
                    dialogs.showOkDialog(AccountView.this, new JLabel("#Connection is not ok:" + e.status().getName()));
                } catch (Throwable throwable)
                {
                    throwable.printStackTrace();
                }

            }
        });

        return task;
    }

    @Action
    public void edit() throws UnitOfWorkCompletionException
    {
        if (!editor.isEditing())
            editor.edit();
        else
        {
            editor.view();

            // Update settings
            model.updateSettings(builder.newInstance());
        }
    }

    @Action
    public void register()
    {
        try
        {
            model.register();
            getActionMap().get("register").setEnabled(false);

            // TODO Show dialog

        } catch (RegistrationException e)
        {
            dialogs.showOkDialog(this, new JLabel("#User could not be registered:" + e.getMessage()));
        }
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        builder = model.settings().buildWith();
        settingsBinder.updateWith(builder.prototype());
        connectedBinder.update();

        getActionMap().get("register").setEnabled(!model.isRegistered());
    }
}
