package se.streamsource.streamflow.client.ui.workspace;

import ca.odell.glazedlists.BasicEventList;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.users.workspace.user.inbox.WorkspaceUserInboxClientResource;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;

import java.util.List;

public class ProjectSelectorModel2 implements EventHandler, Refreshable
{
	List<ListItemValue> items;

    WorkspaceUserInboxClientResource resource;

    private EventHandlerFilter eventFilter;

    private BasicEventList<ListItemValue> list;

	public ProjectSelectorModel2(@Uses WorkspaceUserInboxClientResource resource)
	{
		this.resource = resource;
        eventFilter = new EventHandlerFilter(this, "addedProject", "removedProject", "joinedProject", "leftProject");
        list = new BasicEventList<ListItemValue>();

        refresh();
    }

    public BasicEventList<ListItemValue> getList()
    {
        return list;
    }
    public void refresh()
    {
        try
        {
            this.items = resource.projects().items().get();
            list.clear();
            list.addAll( items );
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
        eventFilter.handleEvent( event );
    }

    public ListItemValue getProjectByName( String projectName )
    {
        for (ListItemValue item : items)
        {
            if (item.description().get().equals(projectName))
                return item;
        }

        return null;
    }
}