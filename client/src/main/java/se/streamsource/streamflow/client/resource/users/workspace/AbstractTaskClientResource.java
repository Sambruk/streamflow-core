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

package se.streamsource.streamflow.client.resource.users.workspace;

import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.client.resource.task.TaskClientResource;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.entity.EntityReference;
import org.restlet.data.Reference;
import org.restlet.Context;
import org.restlet.resource.ResourceException;

/**
 * JAVADOC
 */
public class AbstractTaskClientResource
    extends CommandQueryClientResource
{
    public AbstractTaskClientResource( @Uses Context context, @Uses Reference reference )
    {
        super( context, reference );
    }

    public TaskClientResource task()
    {
        return getResource(getReference().getParentRef().getParentRef().getParentRef().clone().addSegment("task").addSegment( getReference().getLastSegment() ), TaskClientResource.class);
    }

    public void complete() throws ResourceException
    {
        putCommand("complete");
    }

    public void drop() throws ResourceException
    {
        putCommand("drop");
    }

    public void assignToMe() throws ResourceException
    {
        putCommand("assignToMe");
    }

    public void markAsRead() throws ResourceException
    {
        putCommand("markAsRead");
    }

    public void markAsUnread() throws ResourceException
    {
        putCommand("markAsUnread");
    }

    public void delegate(String delegateeId) throws ResourceException
    {
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        builder.prototype().entity().set( EntityReference.parseEntityReference(delegateeId));
        putCommand("delegate", builder.newInstance());
    }

    public void forward(String receiverId) throws ResourceException
    {
        ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
        builder.prototype().entity().set(EntityReference.parseEntityReference(receiverId));
        putCommand("forward", builder.newInstance());
    }
}
