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
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.domain.individual.AccountSettingsValue;
import se.streamsource.streamflow.client.resource.users.shared.user.assignments.SharedUserAssignmentsClientResource;
import se.streamsource.streamflow.client.ui.DetailView;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;

import javax.swing.*;

/**
 * JAVADOC
 */
public class UserAssignmentsNode
        extends DefaultMutableTreeTableNode
        implements DetailView
{
    @Service
    UserAssignmentsView view;

    @Service
    UserAssignmentsModel model;

    @Uses
    private AccountSettingsValue settings;

    public UserAssignmentsNode(@Uses SharedUserAssignmentsClientResource assignmentsClientResource)
    {
        super(assignmentsClientResource, false);
    }

    @Override
    public Object getValueAt(int column)
    {
        return i18n.text(WorkspaceResources.assignments_node);
    }

    SharedUserAssignmentsClientResource assignments()
    {
        return (SharedUserAssignmentsClientResource) getUserObject();
    }

    public JComponent detailView()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    model.setAssignments(assignments());
                } catch (ResourceException e)
                {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

    public AccountSettingsValue getSettings()
    {
        return settings;
    }
}