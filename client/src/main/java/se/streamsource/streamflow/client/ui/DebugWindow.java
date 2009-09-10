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

package se.streamsource.streamflow.client.ui;

import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Action;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.AllEventsSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.EventSourceListener;
import se.streamsource.streamflow.infrastructure.event.source.EventSpecification;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;

import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ActionMap;
import javax.swing.table.DefaultTableModel;

/**
 * JAVADOC
 */
public class DebugWindow
    extends FrameView
    implements EventSourceListener
{
    public JXTable eventTable;
    public DefaultTableModel eventModel;

    public DebugWindow(@Service Application application, @Service EventSource eventSource)
    {
        super(application);

        eventSource.registerListener(this, AllEventsSpecification.INSTANCE);

        eventModel = new DefaultTableModel(new String[]{"Usecase", "Event", "Entity", "Parameters"}, 0);

        eventTable = new JXTable(eventModel);
        eventTable.setEditable(false);

        JXFrame frame = new JXFrame("Debug");
        frame.getContentPane().add(new JScrollPane(eventTable));

        setFrame(frame);

        JToolBar toolbar = new JToolBar();
        ActionMap am = application.getContext().getActionMap(this);
        toolbar.add(am.get("clear"));

        setToolBar(toolbar);

        frame.setSize(400, 400);
    }

    public void eventsAvailable(EventStore source, EventSpecification specification)
    {
        Iterable<DomainEvent> events = source.events(specification, null, Integer.MAX_VALUE);
        for (DomainEvent event : events)
        {
            eventModel.addRow(new String[]{event.usecase().get(), event.name().get(), event.entity().get(), event.parameters().get()});
        }
        eventTable.scrollRowToVisible(eventModel.getRowCount());
    }

    @Action
    public void clear()
    {
        int count = eventModel.getRowCount();
        for (int i = 0; i < count; i++)
            eventModel.removeRow(0);
    }
}
