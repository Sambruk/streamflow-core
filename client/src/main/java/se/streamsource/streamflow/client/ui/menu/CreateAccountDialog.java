/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.menu;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class CreateAccountDialog
        extends JPanel
{
    @Structure
    UnitOfWorkFactory uowf;

    @Service
    IndividualRepository individualRepository;

    @Structure
    ValueBuilderFactory vbf;

    private JTextField accountName;
    private JTextField serverAddress;
    private JTextField userName;
    private JTextField password;

    private JPanel accountPanel;

    public CreateAccountDialog(@Service ApplicationContext context)
    {
        super(new BorderLayout());

        ApplicationActionMap am = context.getActionMap(this);
        setActionMap(am);
        setName("#Enter Settings for new Account");
        accountPanel = new JPanel();
        this.add(accountPanel, BorderLayout.NORTH);

        FormLayout layout = new FormLayout("200dlu", "");

        DefaultFormBuilder builder = new DefaultFormBuilder(layout, accountPanel);

        builder.append(new JLabel("#Account name"));
        accountName = new JTextField();
        builder.append(accountName);
        builder.nextLine();

        builder.append(new JLabel("#Server address"));
        serverAddress = new JTextField();
        builder.append(serverAddress);
        builder.nextLine();

        builder.append(new JLabel("#user name"));
        userName = new JTextField();
        builder.append(userName);
        builder.nextLine();

        builder.append(new JLabel("#Password"));
        password = new JPasswordField();
        builder.append(password);
        builder.nextLine();
    }

    @Action
    public void execute()
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Create Account"));
        ValueBuilder<AccountSettingsValue> settings = vbf.newValueBuilder(AccountSettingsValue.class);

        settings.prototype().name().set(accountName.getText());
        settings.prototype().server().set(serverAddress.getText());
        settings.prototype().userName().set(userName.getText());
        settings.prototype().password().set(password.getText());

        Account account = individualRepository.individual().newAccount();
        account.updateSettings(settings.newInstance());
        try
        {
            uow.complete();
        } catch (UnitOfWorkCompletionException e)
        {
            uow.discard();
        }
        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }

}