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

package se.streamsource.streamflow.client.ui.task;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;

import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.task.TaskGeneralClientResource;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

/**
 * Model for the general info about a task.
 */
public class TaskGeneralModel implements Refreshable, EventListener,
		EventHandler, PropertyChangeListener

{
	EventHandlerFilter eventFilter = new EventHandlerFilter(this, "labelAdded",
			"labelDeleted", "labelUpdated");

	@Uses
	private TaskGeneralClientResource generalClientResource;

	TaskGeneralDTO general;

	public TaskGeneralDTO getGeneral()
	{
		if (general == null)
			refresh();

		return general;
	}

	public void describe(String newDescription)
	{
		try
		{
			generalClientResource.changeDescription(newDescription);
		} catch (ResourceException e)
		{
			throw new OperationException(
					TaskResources.could_not_change_description, e);
		}
	}

	public void changeNote(String newNote)
	{
		try
		{
			generalClientResource.changeNote(newNote);
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_change_note, e);
		}
	}

	public void changeDueOn(Date newDueOn)
	{
		try
		{
			generalClientResource.changeDueOn(newDueOn);
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_change_due_on,
					e);
		}
	}

	public void addLabel(String labelId) throws ResourceException
	{
		try
		{
			generalClientResource.addLabel(labelId);
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_add_label,
					e);
		}
	}

	public void removeLabel(String labelId) throws ResourceException
	{
		try
		{
			generalClientResource.removeLabel(labelId);
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_remove_label,
					e);
		}
	}
	
	public ListValue getOwnerLabels()  
	{
		try
		{
			return generalClientResource.ownerLabels();
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_get_owner_labels,
					e);
		}
	}
	
	public ListValue getOrganizationLabels(StringDTO prefix)
	{
		try
		{
			return generalClientResource.organizationLabels(prefix);
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_get_organisation_labels,
					e);
		}
	}

	public void refresh()
	{
		try
		{
			general = (TaskGeneralDTO) generalClientResource.general()
					.buildWith().prototype();
		} catch (Exception e)
		{
			throw new OperationException(TaskResources.could_not_refresh, e);
		}
	}

	public void notifyEvent(DomainEvent event)
	{

	}

	public boolean handleEvent(DomainEvent event)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		// TODO Auto-generated method stub

	}
}