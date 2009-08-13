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

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * List of accounts
 */
@Mixins(Accounts.AccountsMixin.class)
public interface Accounts
{
    Account newAccount();

    void removeAccount(Account removedAccount);

    void visitAccounts(AccountVisitor visitor);

    interface AccountsState
    {
        @Aggregated
        ManyAssociation<Account> accounts();
    }

    class AccountsMixin
            implements Accounts
    {
        @This
        AccountsState state;

        @Structure
        UnitOfWorkFactory uowf;
        @Structure
        ValueBuilderFactory vbf;

        public Account newAccount()
        {
            AccountSettingsValue settings = vbf.newValue(AccountSettingsValue.class);
            Account account = uowf.currentUnitOfWork().newEntity(Account.class);
            account.updateSettings(settings);
            state.accounts().add(state.accounts().count(), account);
            return account;
        }

        public void removeAccount(Account removedAccount)
        {
            state.accounts().remove(removedAccount);
        }

        public void visitAccounts(AccountVisitor visitor)
        {
            for (Account account : state.accounts())
            {
                visitor.visitAccount(account);
            }
        }
    }
}
