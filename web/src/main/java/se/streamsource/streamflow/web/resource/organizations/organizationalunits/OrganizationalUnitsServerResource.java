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

package se.streamsource.streamflow.web.resource.organizations.organizationalunits;

import org.qi4j.api.entity.EntityReference;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/organizationalunits
 */
public class OrganizationalUnitsServerResource
        extends CommandQueryServerResource
{
    public ListValue organizationalunits()
    {
        String identity = getRequest().getAttributes().get("organization").toString();
        OrganizationalUnits.OrganizationalUnitsState ous = uowf.currentUnitOfWork().get(OrganizationalUnits.OrganizationalUnitsState.class, identity);

        ListValueBuilder builder = new ListValueBuilder(vbf);
        for (OrganizationalUnit ou : ous.organizationalUnits())
        {
            Describable describable = (Describable) ou;
            builder.addListItem(describable.getDescription(), EntityReference.getEntityReference(ou));
        }
        return builder.newList();
    }
}
