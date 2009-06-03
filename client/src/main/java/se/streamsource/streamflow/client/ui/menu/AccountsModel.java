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
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountVisitor;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;

/**
 * JAVADOC
 */
public class AccountsModel
        extends DefaultListModel
{
    @Structure
    ValueBuilderFactory vbf;

    @Service
    IndividualRepository repository;

    @Structure
    UnitOfWorkFactory uowf;

    public void removeAccount(Account account)
    {
        repository.individual().removeAccount(account);
        refresh();
    }

    public void refresh()
    {
        clear();
        uowf.newUnitOfWork();
        final ValueBuilder<ListItemValue> itemBuilder = vbf.newValueBuilder(ListItemValue.class);
        repository.individual().visitAccounts(new AccountVisitor(){

            public void visitAccount(Account account)
            {
                itemBuilder.prototype().description().set(account.settings().name().get());
                itemBuilder.prototype().entity().set(EntityReference.getEntityReference((account)));
                addElement(itemBuilder.newInstance());
            }
        });
    }
}