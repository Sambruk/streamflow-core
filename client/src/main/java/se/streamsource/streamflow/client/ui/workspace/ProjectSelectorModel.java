package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.users.workspace.user.inbox.WorkspaceUserInboxClientResource;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;

import javax.swing.*;
import java.util.List;

public class ProjectSelectorModel extends AbstractListModel implements
		ComboBoxModel, EventListener, EventHandler, Refreshable
{
	List<ListItemValue> items;
	
	ListItemValue selectedItem;

    WorkspaceUserInboxClientResource resource;

    private EventHandlerFilter eventFilter;
	
	public ProjectSelectorModel(@Uses WorkspaceUserInboxClientResource resource)
	{
		this.resource = resource;
        eventFilter = new EventHandlerFilter(this, "addedProject", "removedProject", "joinedProject", "leftProject");
        refresh();
    }

    public Object getElementAt(int index)
	{
		if(items != null && items.size() > 0) 
		{
			return items.get(index);
		}
		return null;
	}

	public int getSize()
	{
		if(items != null)
		{
			return items.size();
		}
		return 0;
	}

	public Object getSelectedItem()
	{
		return selectedItem;
	}

	public void setSelectedItem(Object anItem)
	{
		this.selectedItem = (ListItemValue)anItem;
	}

    public void refresh()
    {
        try
        {
            this.items = resource.projects().items().get();
            fireContentsChanged(this,0,items.size());
        } catch (ResourceException e)
        {
            throw new OperationException(WorkspaceResources.could_not_refresh_projects, e);
        }
    }

    public boolean handleEvent(DomainEvent event)
    {
        this.refresh();
        return true;
    }

    public void notifyEvent(DomainEvent event)
    {
        this.handleEvent(event);
    }
}
