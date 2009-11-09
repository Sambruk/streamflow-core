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

package se.streamsource.streamflow.client.resource.organizations.policy;

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.client.ui.UsersAndGroupsFilter;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * JAVADOC
 */
public class AdministratorsClientResource
        extends CommandQueryClientResource
    implements UsersAndGroupsFilter
{
    public AdministratorsClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public ListValue administrators() throws ResourceException
    {
        return query("administrators", ListValue.class);
    }

    public void addAdministrator(StringDTO value) throws ResourceException
    {
        postCommand("addAdministrator", value);
    }

    public AdministratorClientResource role(String id)
    {
        return getSubResource(id, AdministratorClientResource.class);
    }

    public ListValue findUsers(String query) throws ResourceException
    {
        ValueBuilder<StringDTO> builder = vbf.newValueBuilder(StringDTO.class);
        builder.prototype().string().set(query);
        return query("findUsers", builder.newInstance(), ListValue.class);
    }

    public ListValue findGroups(String query) throws ResourceException
    {
        ValueBuilder<StringDTO> builder = vbf.newValueBuilder(StringDTO.class);
        builder.prototype().string().set(query);
        return query("findGroups", builder.newInstance(), ListValue.class);

    }
}