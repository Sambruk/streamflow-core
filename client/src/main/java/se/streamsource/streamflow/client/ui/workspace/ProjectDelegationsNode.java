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
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.resource.users.shared.projects.delegations.ProjectDelegationsClientResource;
import se.streamsource.streamflow.client.resource.users.shared.user.delegations.UserDelegationsClientResource;
import se.streamsource.streamflow.client.ui.DetailView;

import javax.swing.JComponent;

/**
 * JAVADOC
 */
public class ProjectDelegationsNode
        extends DefaultMutableTreeTableNode
        implements DetailView
{
    @Service
    UserDelegationsView view;

    @Service
    UserDelegationsModel model;

    public ProjectDelegationsNode(@Uses ProjectDelegationsClientResource delegations)
    {
        super(delegations, false);
    }

    @Override
    public Object getValueAt(int column)
    {
        return i18n.text(WorkspaceResources.delegations_node);
    }

    UserDelegationsClientResource delegations()
    {
        return (UserDelegationsClientResource) getUserObject();
    }

    public JComponent detailView() throws ResourceException
    {
        model.setDelegations(delegations());
        return view;
    }
}