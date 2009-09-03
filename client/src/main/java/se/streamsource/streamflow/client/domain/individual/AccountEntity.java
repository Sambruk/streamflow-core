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

package se.streamsource.streamflow.client.domain.individual;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.*;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;
import se.streamsource.streamflow.client.resource.StreamFlowClientResource;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.user.ChangePasswordCommand;
import se.streamsource.streamflow.resource.user.RegisterUserCommand;

/**
 * JAVADOC
 */
@Mixins({AccountEntity.AccountMixin.class})
public interface AccountEntity
        extends Account, EntityComposite
{
    interface AccountState
            extends Describable
    {
        // Settings
        Property<AccountSettingsValue> settings();

        // Registration
        @UseDefaults
        Property<Boolean> registered();
    }

    class AccountMixin
            implements AccountSettings, AccountRegistration, AccountConnection
    {
        @Structure
        ValueBuilderFactory vbf;

        @Structure
        ObjectBuilderFactory obf;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        Account account;

        @This
        AccountState state;

        @This
        Describable description;

        @Service
        IndividualRepository repo;

        // AccountSettings
        public AccountSettingsValue accountSettings()
        {
            return state.settings().get();
        }

        public void updateSettings(AccountSettingsValue newAccountSettings)
        {
            state.settings().set(newAccountSettings);
            description.describe(newAccountSettings.name().get());
        }

        public void changePassword(Restlet client, ChangePasswordCommand changePassword) throws ResourceException
        {
            user(client).changePassword(changePassword);

            AccountSettingsValue settings = state.settings().get().<AccountSettingsValue>buildWith().prototype();
            settings.password().set(changePassword.newPassword().get());

            updateSettings(settings);
        }

        // AccountRegistration
        public void register(Restlet client) throws ResourceException
        {
            ValueBuilder<RegisterUserCommand> commandBuilder = vbf.newValueBuilder(RegisterUserCommand.class);
            commandBuilder.prototype().username().set(state.settings().get().userName().get());
            commandBuilder.prototype().password().set(state.settings().get().password().get());

            ValueBuilder<ContactValue> contactBuilder = vbf.newValueBuilder(ContactValue.class);
            ContactValue contact = contactBuilder.newInstance();
            commandBuilder.prototype().contact().set(contact);

            RegisterUserCommand command = commandBuilder.newInstance();

            StreamFlowClientResource server = server(client);
            server.users().register(command);
            state.registered().set(true);
        }

        public boolean isRegistered()
        {
            return state.registered().get();
        }

        // AccountConnection
        public StreamFlowClientResource server(Restlet client)
        {
            AccountSettingsValue settings = accountSettings();
            Reference serverRef = new Reference(settings.server().get());
            serverRef.addSegment("streamflow").addSegment("v1").addSegment("");

            AuthenticationFilter filter = new AuthenticationFilter(uowf, account);
            filter.setNext(client);

            Context childContext = new Context();
            childContext.setClientDispatcher(filter);
            return obf.newObjectBuilder(StreamFlowClientResource.class).use(childContext, serverRef).newInstance();
        }

        public UserClientResource user(Restlet client)
        {
            return server(client).users().user(accountSettings().userName().get());
        }
    }

    class AuthenticationFilter extends Filter
    {
        private String username;
        private String password;
        private IndividualRepository repo;
        private UnitOfWorkFactory uowf;
        private AccountSettings account;

        public AuthenticationFilter(UnitOfWorkFactory uowf, AccountSettings account)
        {
            this.uowf = uowf;
            this.account = account;
        }

        @Override
        protected int beforeHandle(Request request, Response response)
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            AccountSettingsValue settings;
            if (uow == null)
            {
                uow = uowf.newUnitOfWork();
                settings = uow.get(account).accountSettings();
                uow.discard();
            } else
            {
                settings = uow.get(account).accountSettings();
            }

            request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, settings.userName().get(), settings.password().get()));

            return super.beforeHandle(request, response);
        }
    }
}
