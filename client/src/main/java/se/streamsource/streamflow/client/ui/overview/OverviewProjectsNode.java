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

package se.streamsource.streamflow.client.ui.overview;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.users.overview.projects.OverviewProjectClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.OverviewProjectsClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.assignments.OverviewProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.waitingfor.OverviewProjectWaitingForClientResource;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * JAVADOC
 */
public class OverviewProjectsNode
        extends DefaultMutableTreeNode
        implements Refreshable
{
    private AccountModel account;
    private ObjectBuilderFactory obf;

    public OverviewProjectsNode(@Uses AccountModel account,
                                @Structure final ObjectBuilderFactory obf) throws Exception
    {
        super(account);
        this.account = account;
        this.obf = obf;
    }

    @Override
    public OverviewNode getParent()
    {
        return (OverviewNode) super.getParent();
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
            OverviewProjectsClientResource projectsClientResource = user.overview().projects();
            ListValue projects = projectsClientResource.listProjects();

            super.removeAllChildren();

            for (ListItemValue project : projects.items().get())
            {
                OverviewProjectClientResource projectClientResource = projectsClientResource.project(project.entity().get().identity());
                OverviewProjectAssignmentsClientResource projectAssignmentsClientResource = projectClientResource.assignments();
                OverviewProjectWaitingForClientResource projectWaitingforClientResource = projectClientResource.waitingFor();
                add(obf.newObjectBuilder(OverviewProjectNode.class).use(projectClientResource,
                        projectAssignmentsClientResource,
                        projectWaitingforClientResource,
                        account.tasks(),
                        project.description().get()).newInstance());
            }
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh,  e);
        }
    }
}