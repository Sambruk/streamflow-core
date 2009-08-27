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

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.organizations.groups.GroupClientResource;
import se.streamsource.streamflow.client.resource.organizations.groups.participants.ParticipantClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.AbstractListModel;

/**
 * JAVADOC
 */
public class GroupModel
        extends AbstractListModel
{
    public ListValue list;

    public GroupModel(@Uses GroupClientResource group)
    {
        this.group = group;
    }

    @Uses private GroupClientResource group;

    public int getSize()
    {
        return list == null ? 0 : list.items().get().size();
    }

    public Object getElementAt(int index)
    {
        return list.items().get().get(index);
    }


    public void addParticipants(Iterable<String> participants)
    {
        try
        {
            for (String value: participants)
            {
                ParticipantClientResource participant = group.participants().participant(value);
                participant.put(null);
            }
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_add_participants, e);
        }
    }

    public void removeParticipant(String participant)
    {
        try
        {
            group.participants().participant(participant).delete();
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_remove_participant, e);
        }

    }

    private void refresh() throws ResourceException
    {
        list = group.participants().participants();
        fireContentsChanged(this, 0, getSize());
    }
}