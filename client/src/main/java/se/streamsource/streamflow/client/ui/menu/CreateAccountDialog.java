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
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Restlet;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.PASSWORD;
import static se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder.Fields.TEXTFIELD;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.*;

/**
 * JAVADOC
 */
public class CreateAccountDialog
        extends JPanel
{
    private AccountSettingsValue settings;

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    IndividualRepository individualRepository;

    @Service
    StreamFlowApplication controller;

    @Service
    Restlet client;

    ValueBuilderFactory vbf;

    private StateBinder accountBinder;
    private ValueBuilder<AccountSettingsValue> accountBuilder;

    public CreateAccountDialog(@Service ApplicationContext context,
                               @Structure ValueBuilderFactory vbf)
    {
        setActionMap(context.getActionMap(this));
        this.vbf = vbf;

        FormLayout layout = new FormLayout(
                "200dlu",
                "");                                      // add rows dynamically
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
        builder.setDefaultDialogBorder();

        accountBinder = new StateBinder();
        accountBinder.setResourceMap(context.getResourceMap(getClass()));
        AccountSettingsValue template = accountBinder.bindingTemplate(AccountSettingsValue.class);

        BindingFormBuilder bb = new BindingFormBuilder(builder, accountBinder);

        bb.appendLine(AdministrationResources.create_account_name, TEXTFIELD, template.name())
                .appendLine(AdministrationResources.create_account_server, TEXTFIELD, template.server())
                .appendLine(AdministrationResources.create_account_username, TEXTFIELD, template.userName())
                .appendLine(AdministrationResources.create_account_password, PASSWORD, template.password());


        accountBuilder = vbf.newValueBuilder(AccountSettingsValue.class);

        // for the demo this has been pre-filled
        accountBuilder.prototype().server().set("http://streamflow.doesntexist.com/streamflow");

        accountBinder.updateWith(accountBuilder.prototype());
    }

    @Action
    public void execute()
    {
        settings = accountBuilder.newInstance();
        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }

    public AccountSettingsValue settings()
    {
        return settings;
    }
}