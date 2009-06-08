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

package se.streamsource.streamflow.client.ui.administration.groups;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.organizations.groups.GroupClientResource;
import se.streamsource.streamflow.client.resource.organizations.groups.participants.ParticipantClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.DefaultListModel;
import java.util.Collection;

/**
 * JAVADOC
 */
public class GroupModel
        extends DefaultListModel
{
    @Structure
    ValueBuilderFactory vbf;

    public GroupClientResource getGroup()
    {
        return group;
    }

    private GroupClientResource group;

    public void setGroup(GroupClientResource resource)
    {
        this.group = resource;
        refresh();
    }

    public void removeParticipant(EntityReference participant)
    {

        try
        {
            group.participants().participant(participant.identity()).delete();
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_remove_participant, e);
        }

    }

    private void refresh()
    {
        clear();
        try
        {
            for (ListItemValue value : group.participants().participants().items().get())
            {
                addElement(value);
            }
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh_list_of_participants, e);
        }
    }


    public void addParticipants(Collection<ListItemValue> participants)
    {
        try
        {
            for (ListItemValue value: participants)
            {
                ParticipantClientResource participant = group.participants().participant(value.entity().get().identity());
                participant.put(null);
            }
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_add_participants, e);
        }
    }
}