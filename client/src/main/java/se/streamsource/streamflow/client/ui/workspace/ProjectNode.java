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
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.resource.users.workspace.projects.UserProjectClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.assignments.ProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.delegations.ProjectDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.inbox.ProjectInboxClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor.ProjectWaitingforClientResource;

/**
 * JAVADOC
 */
public class ProjectNode
        extends DefaultMutableTreeTableNode
{
    private LabelsModel labelsModel;

    @Uses String projectName;

    public ProjectNode(@Uses UserProjectClientResource userProjectClientResource,
                             @Structure ObjectBuilderFactory obf)
    {
        super(userProjectClientResource);

        ProjectInboxClientResource projectInboxClientResource = userProjectClientResource.inbox();
        add(obf.newObjectBuilder(ProjectInboxNode.class).use(projectInboxClientResource).newInstance());

        ProjectAssignmentsClientResource projectAssignmentsClientResource = userProjectClientResource.assignments();
        add(obf.newObjectBuilder(UserAssignmentsNode.class).use(projectAssignmentsClientResource).newInstance());

        ProjectDelegationsClientResource projectDelegationsClientResource = userProjectClientResource.delegations();
        add(obf.newObjectBuilder(UserDelegationsNode.class).use(projectDelegationsClientResource).newInstance());

        ProjectWaitingforClientResource projectWaitingforClientResource = userProjectClientResource.waitingFor();
        add(obf.newObjectBuilder(UserWaitingForNode.class).use(projectWaitingforClientResource).newInstance());

        labelsModel = obf.newObjectBuilder(LabelsModel.class).use(userProjectClientResource.labels()).newInstance();
    }

    public LabelsModel labelsModel()
    {
        return labelsModel;
    }

    @Override
    public Object getValueAt(int column)
    {
        return projectName;
    }
}