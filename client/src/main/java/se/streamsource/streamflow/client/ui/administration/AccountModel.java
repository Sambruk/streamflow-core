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

import org.qi4j.api.entity.Entity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.restlet.Restlet;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.RegistrationException;
import se.streamsource.streamflow.client.resource.StreamFlowClientResource;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.infrastructure.application.TreeValue;

import java.util.Observable;

/**
 * JAVADOC
 */
public class AccountModel
    extends Observable
{
    @Service
    Restlet client;

    @Uses
    Account account;

    public AccountSettingsValue settings()
    {
        return account.settings();
    }

    public void updateSettings(AccountSettingsValue value) throws UnitOfWorkCompletionException
    {
        account.updateSettings(value);
        ((Entity)account).unitOfWork().apply();
        setChanged();
        notifyObservers();
    }

    public void register() throws RegistrationException
    {
        account.register(client);
    }

    public String test()
    {
        return account.server(client).version();
    }

    public boolean isRegistered()
    {
        return account.isRegistered();
    }

    public UserClientResource userResource()
    {
        return account.user(client);
    }

    public StreamFlowClientResource serverResource()
    {
        return account.server(client);
    }

    public TreeValue organizations() throws ResourceException
    {
        return account.user(client).administration().organizations();
    }
}
