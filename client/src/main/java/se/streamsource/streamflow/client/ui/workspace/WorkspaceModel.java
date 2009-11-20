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
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;

import javax.swing.tree.DefaultTreeModel;
import java.util.logging.Logger;

/**
 * JAVADOC
 */
public class WorkspaceModel
        extends DefaultTreeModel
    implements EventListener, EventHandler
{
    public EventHandlerFilter eventHandlerFilter;

    public WorkspaceModel(@Uses WorkspaceNode node)
    {
        super(node);

        eventHandlerFilter = new EventHandlerFilter(this, "joinedProject","leftProject","joinedGroup","leftGroup",
                "createdProject","removedProject");
    }

    @Override
    public WorkspaceNode getRoot()
    {
        return (WorkspaceNode) super.getRoot();
    }

    public void notifyEvent( DomainEvent event )
    {
        getRoot().notifyEvent(event);

        eventHandlerFilter.handleEvent( event );
    }

    public boolean handleEvent( DomainEvent event )
    {
        Logger.getLogger("workspace").info("Refresh project list");
        getRoot().getProjectsNode().refresh();
        reload(getRoot().getProjectsNode());

        return true;
    }
}
