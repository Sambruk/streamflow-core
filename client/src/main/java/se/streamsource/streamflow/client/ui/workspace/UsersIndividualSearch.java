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
package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.Restlet;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountVisitor;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSelectionView;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import java.util.ArrayList;
import java.util.List;

public class UsersIndividualSearch
{

    @Uses
    TableSelectionView view;

    @Service
    IndividualRepository individual;

    @Service
    Restlet client;

    
    public void search()
    {
        final List<ListValue> results = new ArrayList<ListValue>();
        individual.individual().visitAccounts(new AccountVisitor(){

            public void visitAccount(Account account)
            {

                try
                {
                    results.add(account.user(client).findUsers(view.searchText()));
                } catch (ResourceException e)
                {
                    e.printStackTrace();
                }
            }
        });
        System.out.println("User accounts: " +results.size());
        if (results.size() > 0)
        {
            view.getModel().setModel(results.get(0));
        }
    }

}