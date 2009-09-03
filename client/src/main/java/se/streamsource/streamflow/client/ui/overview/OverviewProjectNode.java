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
import se.streamsource.streamflow.client.resource.users.overview.projects.OverviewProjectClientResource;
import se.streamsource.streamflow.client.resource.users.overview.projects.assignments.OverviewProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor.ProjectWaitingforClientResource;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * JAVADOC
 */
public class OverviewProjectNode
        extends DefaultMutableTreeNode
{
    @Uses
    String projectName;

    public OverviewProjectNode(@Uses OverviewProjectClientResource projectClientResource,
                               @Structure ObjectBuilderFactory obf)
    {
        super(projectClientResource);

        OverviewProjectAssignmentsClientResource projectAssignmentsClientResource = projectClientResource.assignments();
        add(obf.newObjectBuilder(OverviewProjectAssignmentsNode.class).use(projectAssignmentsClientResource).newInstance());

        ProjectWaitingforClientResource projectWaitingforClientResource = projectClientResource.waitingFor();
        add(obf.newObjectBuilder(OverviewProjectWaitingForNode.class).use(projectWaitingforClientResource).newInstance());
    }

    public String projectName()
    {
        return projectName;
    }

    @Override
    public OverviewProjectsNode getParent()
    {
        return (OverviewProjectsNode) super.getParent();
    }
}