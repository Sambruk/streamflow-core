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

package se.streamsource.streamflow.client.resource.organizations;

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Context;
import org.restlet.resource.ResourceException;
import org.restlet.data.Reference;
import se.streamsource.streamflow.client.resource.organizations.organizationalunits.OrganizationalUnitClientResource;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.DescriptionValue;

/**
 * JAVADOC
 */
public class OrganizationClientResource
        extends OrganizationalUnitClientResource
{
    @Structure
    protected ValueBuilderFactory vbf;


    public OrganizationClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public ListValue findUsers(String participantName) throws ResourceException
    {
        ValueBuilder<DescriptionValue> builder = vbf.newValueBuilder(DescriptionValue.class);
        builder.prototype().description().set(participantName);
        return query("findUsers", builder.newInstance(), ListValue.class);
    }

    public ListValue findGroups(String groupName) throws ResourceException
    {
        ValueBuilder<DescriptionValue> builder = vbf.newValueBuilder(DescriptionValue.class);
        builder.prototype().description().set(groupName);
        return query("findGroups", builder.newInstance(), ListValue.class);        
    }
}