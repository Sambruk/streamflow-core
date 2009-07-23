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

package se.streamsource.streamflow.client.ui.workspace;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.Restlet;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.inbox.UserInboxClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.assignments.SharedUserAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.delegations.SharedUserDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.waitingfor.SharedUserWaitingForClientResource;

/**
 * JAVADOC
 */
public class UserNode
        extends DefaultMutableTreeTableNode
{
    public UserNode(@Uses Account account,
                          @Service Restlet client,
                          @Structure ObjectBuilderFactory obf) throws ResourceException
    {
        super(account);

        UserClientResource user = account.user(client);
        UserInboxClientResource userInboxResource = user.shared().user().inbox();
        add(obf.newObjectBuilder(UserInboxNode.class).use(account.settings(), userInboxResource).newInstance());

        SharedUserAssignmentsClientResource userAssignmentsResource = user.shared().user().assignments();
        add(obf.newObjectBuilder(UserAssignmentsNode.class).use(account.settings(), userAssignmentsResource).newInstance());

        SharedUserDelegationsClientResource userDelegationsResource = user.shared().user().delegations();
        add(obf.newObjectBuilder(UserDelegationsNode.class).use(account.settings(), userDelegationsResource).newInstance());

        SharedUserWaitingForClientResource userWaitingForResource = user.shared().user().waitingFor();
        add(obf.newObjectBuilder(UserWaitingForNode.class).use(account.settings(), userWaitingForResource).newInstance());
    }

    @Override
    public Object getValueAt(int column)
    {
        return i18n.text(WorkspaceResources.user_node);
    }
}