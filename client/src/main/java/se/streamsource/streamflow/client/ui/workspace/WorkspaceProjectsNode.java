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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.LabelsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.WorkspaceProjectClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.assignments.ProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.delegations.ProjectDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.inbox.ProjectInboxClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor.ProjectWaitingforClientResource;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * JAVADOC
 */
public class WorkspaceProjectsNode
        extends DefaultMutableTreeNode
        implements Refreshable
{
    private AccountModel account;
    private ObjectBuilderFactory obf;

    public WorkspaceProjectsNode(@Uses AccountModel account,
                                 @Structure final ObjectBuilderFactory obf) throws Exception
    {
        super(account);
        this.account = account;
        this.obf = obf;
    }

    @Override
    public WorkspaceNode getParent()
    {
        return (WorkspaceNode) super.getParent();
    }

    @Override
    public boolean isLeaf()
    {
        return false;
    }

    @Override
    public boolean getAllowsChildren()
    {
        return true;
    }

    public void refresh()
    {
        try
        {
            se.streamsource.streamflow.client.resource.users.UserClientResource user = account.userResource();
            ListValue projects = user.workspace().projects().listProjects();

            super.removeAllChildren();

            for (ListItemValue project : projects.items().get())
            {
                WorkspaceProjectClientResource workspaceProjectClientResource = user.workspace().projects().project(project.entity().get().identity());
                ProjectInboxClientResource projectInboxClientResource = workspaceProjectClientResource.inbox();
                ProjectAssignmentsClientResource projectAssignmentsClientResource = workspaceProjectClientResource.assignments();
                ProjectDelegationsClientResource projectDelegationsClientResource = workspaceProjectClientResource.delegations();
                ProjectWaitingforClientResource projectWaitingforClientResource = workspaceProjectClientResource.waitingFor();
                LabelsClientResource labels = workspaceProjectClientResource.labels();

                add(obf.newObjectBuilder(WorkspaceProjectNode.class).use(workspaceProjectClientResource,
                        projectInboxClientResource,
                        projectAssignmentsClientResource,
                        projectDelegationsClientResource,
                        projectWaitingforClientResource,
                        labels,
                        account.tasks(),
                        project.description().get()).newInstance());
            }
        } catch (ResourceException e)
        {
            throw new OperationException(WorkspaceResources.could_not_refresh_projects, e);
        }
    }
}