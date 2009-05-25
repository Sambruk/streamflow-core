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

package se.streamsource.streamflow.web.resource.users.administration;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.web.domain.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * JAVADOC
 */
public class UserAdministrationServerResource
        extends CommandQueryServerResource
{
    @Structure
    ValueBuilderFactory vbf;

    public TreeValue organizations()
    {
        ValueBuilder<TreeValue> listBuilder = vbf.newValueBuilder(TreeValue.class);
        String id = (String) getRequest().getAttributes().get("user");
        OrganizationParticipations organizationParticipations = uowf.currentUnitOfWork().get(OrganizationParticipations.class, id);
        List<TreeNodeValue> list = listBuilder.prototype().roots().get();
        OrganizationParticipations.OrganizationParticipationsState state = (OrganizationParticipations.OrganizationParticipationsState) organizationParticipations;
        addOrganizationalUnits(state.organizations(), list);
        return listBuilder.newInstance();
    }

    private void addOrganizationalUnits(Iterable<? extends OrganizationalUnit> organizations, List<TreeNodeValue> list)
    {
        for (OrganizationalUnit organization : organizations)
        {
            OrganizationalUnits.OrganizationalUnitsState ou = (OrganizationalUnits.OrganizationalUnitsState) organization;
            ValueBuilder<TreeNodeValue> valueBuilder = vbf.newValueBuilder(TreeNodeValue.class);
            TreeNodeValue itemValue = valueBuilder.prototype();
            itemValue.description().set(((Describable) organization).getDescription());
            itemValue.entity().set(EntityReference.getEntityReference(organization));
            List<TreeNodeValue> subOrgs = itemValue.children().get();
            addOrganizationalUnits(((OrganizationalUnits.OrganizationalUnitsState) organization).organizationalUnits(), subOrgs);
            list.add(valueBuilder.newInstance());
        }
    }
}
