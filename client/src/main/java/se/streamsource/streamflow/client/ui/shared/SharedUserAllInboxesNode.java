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

package se.streamsource.streamflow.client.ui.shared;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.Restlet;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.domain.individual.AccountVisitor;
import se.streamsource.streamflow.client.domain.individual.Individual;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.inbox.SharedUserInboxClientResource;

/**
 * JAVADOC
 */
public class SharedUserAllInboxesNode
        extends DefaultMutableTreeTableNode
{
    ObjectBuilderFactory obf;

    private Restlet client;

    public SharedUserAllInboxesNode(@Uses Individual individual, @Service Restlet client, @Structure ObjectBuilderFactory obf)
    {
        super(individual);
        this.client = client;
        this.obf = obf;
        refresh();
    }


    public void refresh()
    {
        Individual individual = (Individual) getUserObject();
        individual.visitAccounts(new AccountVisitor()
        {
            public void visitAccount(Account account)
            {
                try
                {
                    UserClientResource user = account.user(client);
                    SharedUserInboxClientResource userInboxResource = user.shared().user().inbox();
                    add(obf.newObjectBuilder(SharedUserInboxNode.class).use(account.settings(), userInboxResource).newInstance());
                } catch (ResourceException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public Object getValueAt(int column)
    {
        return i18n.text(SharedResources.inboxes_node);
    }
}