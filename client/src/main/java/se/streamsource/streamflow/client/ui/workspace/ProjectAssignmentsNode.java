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
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.resource.users.workspace.projects.assignments.ProjectAssignmentsClientResource;
import se.streamsource.streamflow.client.resource.users.workspace.user.assignments.UserAssignmentsClientResource;
import se.streamsource.streamflow.client.ui.DetailView;

import javax.swing.JComponent;

/**
 * JAVADOC
 */
public class ProjectAssignmentsNode
        extends DefaultMutableTreeTableNode
        implements DetailView
{
    @Uses
    UserAssignmentsModel model;

    public ProjectAssignmentsNode(@Uses ProjectAssignmentsClientResource assignments)
    {
        super(assignments, false);
    }

    @Override
    public Object getValueAt(int column)
    {
        return i18n.text(WorkspaceResources.assignments_node);
    }

    UserAssignmentsClientResource assignments()
    {
        return (UserAssignmentsClientResource) getUserObject();
    }

    public JComponent detailView() throws ResourceException
    {
/*
        model.setAssignments(assignments());
        return view;
*/
        return null;
    }
}