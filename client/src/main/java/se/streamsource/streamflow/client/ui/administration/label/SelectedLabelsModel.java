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

package se.streamsource.streamflow.client.ui.administration.label;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * Management of selected labels on a tasktype, project or OU level
 */
public class SelectedLabelsModel
        implements EventListener, Refreshable
{
    @Uses
    CommandQueryClient client;

    BasicEventList<ListItemValue> eventList = new BasicEventList<ListItemValue>( );

    @Structure
    ValueBuilderFactory vbf;

    private ListValue list;

    public EventList<ListItemValue> getLabelList()
    {
        return eventList;
    }

    public void refresh()
    {
        try
        {
            // Get label list
            ListValue newList = client.query( "selectedlabels", ListValue.class );

            if (list == null || !newList.equals( list ))
            {
                eventList.clear();
                eventList.addAll( newList.items().get() );
                list = newList;
            }

        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh_list_of_labels, e);
        }
    }

    public EventList<ListItemValue> getPossibleLabels()
    {
        try
        {
            BasicEventList<ListItemValue> possibleLabels = new BasicEventList<ListItemValue>();
            possibleLabels.addAll( client.query( "possiblelabels", ListValue.class ).items().get() );
            return possibleLabels;
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh, e);
        }
    }

    public void createLabel(String description)
    {
        try
        {
            ValueBuilder<StringDTO> builder = vbf.newValueBuilder(StringDTO.class);
            builder.prototype().string().set(description);
            client.postCommand("createlabel",builder.newInstance());
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_create_label, e);
        }
    }

    public void addLabel( EntityReference identity)
    {
        try
        {
            ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class);
            builder.prototype().entity().set(identity);
            client.postCommand( "addlabel",  builder.newInstance());
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_add_label, e);
        }
    }

    public void removeLabel( EntityReference identity)
    {
        try
        {
            client.getSubClient( identity.identity() ).deleteCommand();
        } catch (ResourceException e)
        {
            throw new OperationException( AdministrationResources.could_not_remove_label, e);
        }
    }

    public void notifyEvent( DomainEvent event )
    {
    }
}