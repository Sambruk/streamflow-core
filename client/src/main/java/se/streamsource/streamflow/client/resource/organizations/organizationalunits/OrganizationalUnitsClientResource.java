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

package se.streamsource.streamflow.client.resource.organizations.organizationalunits;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;

import java.util.List;

/**
 * JAVADOC
 */
public class OrganizationalUnitsClientResource
        extends CommandQueryClientResource
{
    public OrganizationalUnitsClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public ListValue organizationalUnits() throws ResourceException
    {
        return query("organizationalUnits", ListValue.class);
    }

    public void createOrganizationalUnit(StringDTO value) throws ResourceException
    {
        postCommand("createOrganizationalUnit", value);
    }

    public void removeOrganizationalUnit(EntityReferenceDTO entity) throws ResourceException
    {
        postCommand("removeOrganizationalUnit", entity);
    }

    public OrganizationalUnitClientResource organizationalUnit(String id)
    {
        List<String> segments = getReference().getSegments();
        if (segments.get( segments.size()-3 ).equals("organizations"))
            return getResource(getReference().clone().addSegment(id), OrganizationalUnitClientResource.class);
        else
        {
            Reference ref = new Reference( getReference(), "../../" + id );
            return getResource( ref, OrganizationalUnitClientResource.class);
        }
    }
}