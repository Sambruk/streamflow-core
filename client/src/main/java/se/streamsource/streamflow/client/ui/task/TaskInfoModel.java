/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.resource.task.TaskValue;

import java.util.Observable;

/**
 * Model for the quick info about a task.
 */
public class TaskInfoModel extends Observable implements Refreshable,
		EventListener, EventVisitor

{
	EventVisitorFilter eventFilter;

	private CommandQueryClient client;

	TaskValue taskValue;

   public TaskInfoModel(@Uses CommandQueryClient client)
	{
		this.client = client;
		eventFilter = new EventVisitorFilter(client.getReference().getLastSegment(), this, "sentTo", "changedTaskType", "changedDescription", "assignedTo", "unassigned", "changedStatus");
	}

	public TaskValue getInfo()
	{
		if (taskValue == null)
			refresh();

		return taskValue;
	}

	public void refresh()
	{
		try
		{
			taskValue = client.query("info", TaskValue.class);

			setChanged();
			notifyObservers();

		} catch (Exception e)
		{
			throw new OperationException(TaskResources.could_not_refresh, e);
		}
	}

	public void notifyEvent(DomainEvent event)
	{
		eventFilter.visit(event);
	}

	public boolean visit(DomainEvent event)
	{
		refresh();
		return true;
	}

}