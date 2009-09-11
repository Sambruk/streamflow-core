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
import org.qi4j.api.injection.scope.Service;

import javax.swing.tree.DefaultTreeModel;

import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;
import se.streamsource.streamflow.infrastructure.event.source.AllEventsSpecification;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class WorkspaceModel
        extends DefaultTreeModel
{
    public WorkspaceModel(@Uses WorkspaceNode node, @Service EventSource source)
    {
        super(node);

        source.registerListener(new EventSourceListener()
        {
            public void eventsAvailable(EventStore source, EventSpecification specification)
            {
                for (DomainEvent domainEvent : source.events(specification, null, Integer.MAX_VALUE))
                {
                    if (domainEvent.name().get().equals("projectRemoved"))
                    {
                        Logger.getLogger("workspace").info("Refresh project list");
                        getRoot().getProjectsNode().refresh();
                    }
                }
            }
        }, AllEventsSpecification.INSTANCE);
    }

    @Override
    public WorkspaceNode getRoot()
    {
        return (WorkspaceNode) super.getRoot();
    }
}
