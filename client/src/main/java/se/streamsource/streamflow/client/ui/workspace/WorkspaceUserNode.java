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

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * JAVADOC
 */
public class WorkspaceUserNode
        extends DefaultMutableTreeNode
    implements EventListener
{
    private LabelsModel labelsModel;
    @Uses private UserClientResource user;

    public WorkspaceUserNode(@Uses AccountModel account,
                             @Uses WorkspaceUserInboxNode inbox,
                             @Uses WorkspaceUserAssignmentsNode assignments,
                             @Uses WorkspaceUserDelegationsNode delegations,
                             @Uses WorkspaceUserWaitingForNode waitingFor,
                             @Uses LabelsModel labels) throws ResourceException
    {
        super(account);

        add(inbox);

        add(assignments);

        add(delegations);

        add(waitingFor);

        labelsModel = labels;
    }

    public LabelsModel labelsModel()
    {
        return labelsModel;
    }

    public ListValue findUsers(String name) throws ResourceException
    {
        return user.findUsers(name);
    }

    public ListValue findProjects(String name) throws ResourceException
    {
        return user.findProjects(name);
    }

    public void notifyEvent( DomainEvent event )
    {
        for (Object child : children)
        {
            EventListener listener = (EventListener) child;
            listener.notifyEvent( event );
        }
    }
}