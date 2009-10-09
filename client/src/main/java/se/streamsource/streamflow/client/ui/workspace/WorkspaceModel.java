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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.OnEvents;

import javax.swing.tree.DefaultTreeModel;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class WorkspaceModel
        extends DefaultTreeModel
{
    public EventSourceListener subscriber;

    public WorkspaceModel(@Uses WorkspaceNode node, @Service EventSource source)
    {
        super(node);

        // Reload project list whenever someone join or leave a project/group
        subscriber = new OnEvents("joinedProject","leftProject","joinedGroup","leftGroup")
        {
            public void run()
            {
                Logger.getLogger("workspace").info("Refresh project list");
                getRoot().getProjectsNode().refresh();
                reload(getRoot().getProjectsNode());
            }
        };

        source.registerListener(subscriber);
    }

    @Override
    public WorkspaceNode getRoot()
    {
        return (WorkspaceNode) super.getRoot();
    }
}
