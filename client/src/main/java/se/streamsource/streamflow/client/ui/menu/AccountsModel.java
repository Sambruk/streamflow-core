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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Restlet;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.domain.individual.AccountVisitor;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.AbstractListModel;
import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
public class AccountsModel
        extends AbstractListModel
{
    @Structure
    ValueBuilderFactory vbf;

    @Structure
    ObjectBuilderFactory obf;

    @Service
    IndividualRepository repository;

    @Structure
    UnitOfWorkFactory uowf;

    @Service
    Restlet client;

    List<ListItemValue> accounts = new ArrayList<ListItemValue>();

    WeakModelMap<String, AccountModel> models = new WeakModelMap<String, AccountModel>()
    {
        protected AccountModel newModel(String key)
        {
            UnitOfWork uow = uowf.newUnitOfWork();
            Account acc = uow.get(Account.class, key);
            uow.discard();
            return obf.newObjectBuilder(AccountModel.class).use(acc).newInstance();
        }
    };

    public void init(@Service IndividualRepository repository)
    {
        refresh();
    }

    public int getSize()
    {
        return accounts.size();
    }

    public ListItemValue getElementAt(int index)
    {
        return accounts.get(index);
    }

    public AccountModel accountModel(int index)
    {
        String id = accounts.get(index).entity().get().identity();
        return models.get(id);
    }

    public void newAccount(AccountSettingsValue accountSettingsValue) throws UnitOfWorkCompletionException, ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork();

        Account account = repository.individual().newAccount();
        account.updateSettings(accountSettingsValue);

        account.register(client);
        uow.complete();

        refresh();

        fireIntervalAdded(this, accounts.size(), accounts.size());
    }

    public void removeAccount(int index) throws UnitOfWorkCompletionException
    {
        accountModel(index).remove();
        accounts.remove(index);
        fireContentsChanged(this, 0, accounts.size());
    }

    private void refresh()
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        final ValueBuilder<ListItemValue> itemBuilder = vbf.newValueBuilder(ListItemValue.class);
        repository.individual().visitAccounts(new AccountVisitor(){

            public void visitAccount(Account account)
            {
                itemBuilder.prototype().description().set(account.settings().name().get());
                itemBuilder.prototype().entity().set(EntityReference.getEntityReference((account)));
                accounts.add(itemBuilder.newInstance());
            }
        });
        uow.discard();
    }
}