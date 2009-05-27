/*
 * Copyright (c) 2009, Rickard √ñberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.resource.organizations.projects;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.members.MembersClientResource;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.DescriptionValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceValue;

/**
 * JAVADOC
 */
public class ProjectClientResource
        extends CommandQueryClientResource
{
    public ProjectClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public ListValue participants() throws ResourceException
    {
        return query("participants", ListValue.class);
    }

    public MembersClientResource members()
    {
        return getSubResource("members", MembersClientResource.class);
    }

    public void addMember(EntityReference participant) throws ResourceException
    {
        ValueBuilder<EntityReferenceValue> builder = vbf.newValueBuilder(EntityReferenceValue.class);
        builder.prototype().entity().set(participant);
        putCommand("addMember", builder.newInstance());
    }

    public EntityReferenceValue findParticipant(String participant) throws ResourceException
    {
        ValueBuilder<DescriptionValue> builder = vbf.newValueBuilder(DescriptionValue.class);
        builder.prototype().description().set(participant);
        return query("findParticipant", builder.newInstance(), EntityReferenceValue.class);
    }

    public EntityReferenceValue findRole(String roleName) throws ResourceException
    {
        ValueBuilder<DescriptionValue> builder = vbf.newValueBuilder(DescriptionValue.class);
        builder.prototype().description().set(roleName);
        return query("findRole", builder.newInstance(), EntityReferenceValue.class);
    }

    public ListValue findParticipants(String participantName) throws ResourceException
    {
        ValueBuilder<DescriptionValue> builder = vbf.newValueBuilder(DescriptionValue.class);
        builder.prototype().description().set(participantName);
        return query("findParticipants", builder.newInstance(), ListValue.class);
    }
}