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
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.Restlet;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.Account;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.assignments.UserAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.delegations.UserDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.inbox.UserInboxClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.waitingfor.UserWaitingForClientResource;
import se.streamsource.streamflow.infrastructure.application.ListValue;

/**
 * JAVADOC
 */
public class UserNode
        extends DefaultMutableTreeTableNode
{
    private LabelsModel labelsModel;
    public UserClientResource user;

    public UserNode(@Uses Account account,
                          @Service Restlet client,
                          @Structure ObjectBuilderFactory obf) throws ResourceException
    {
        super(account);

        user = account.user(client);

        UserInboxClientResource userInboxResource = user.workspace().user().inbox();
        add(obf.newObjectBuilder(UserInboxNode.class).use(userInboxResource).newInstance());

        UserAssignmentsClientResource userAssignmentsResource = user.workspace().user().assignments();
        add(obf.newObjectBuilder(UserAssignmentsNode.class).use(userAssignmentsResource).newInstance());

        UserDelegationsClientResource userDelegationsResource = user.workspace().user().delegations();
        add(obf.newObjectBuilder(UserDelegationsNode.class).use(userDelegationsResource).newInstance());

        UserWaitingForClientResource userWaitingForResource = user.workspace().user().waitingFor();
        add(obf.newObjectBuilder(UserWaitingForNode.class).use(userWaitingForResource).newInstance());

        labelsModel = obf.newObjectBuilder(LabelsModel.class).use(user.workspace().user().labels()).newInstance();
    }

    public LabelsModel labelsModel()
    {
        return labelsModel;
    }

    public ListValue findUsers(String name) throws ResourceException
    {
        return user.findUsers(name);
    }

    @Override
    public Object getValueAt(int column)
    {
        return i18n.text(WorkspaceResources.user_node);
    }
}