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

package se.streamsource.streamflow.web.domain.organization;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.Event;

/**
 * An organizational unit represents a part of an organization.
 */
@Mixins(OrganizationalUnit.OrganizationalUnitMixin.class)
public interface OrganizationalUnit
{
    Organization getOrganization();

    void moveOrganizationalUnit(OrganizationalUnit from, OrganizationalUnit to);

    void mergeOrganizationalUnit(OrganizationalUnit from, OrganizationalUnit to);

    interface OrganizationalUnitState
    {
        Association<Organization> organization();

        @Event
        void organizationalUnitMoved(DomainEvent event, OrganizationalUnit from, OrganizationalUnit to);

        @Event
        void organizationalUnitMerged(DomainEvent event, OrganizationalUnit from, OrganizationalUnit to);
    }

    abstract class OrganizationalUnitMixin
            implements OrganizationalUnit, OrganizationalUnitState
    {
        @This
        OrganizationalUnitState state;

        public Organization getOrganization()
        {
            return state.organization().get();
        }

        public void moveOrganizationalUnit(OrganizationalUnit from, OrganizationalUnit to)
        {
            OrganizationalUnitEntity uoe = (OrganizationalUnitEntity) state;
            OrganizationalUnitEntity target = (OrganizationalUnitEntity) to;
            OrganizationalUnitEntity source = (OrganizationalUnitEntity) from;
            if (uoe.identity().get().equals(target.identity().get()))
            {
                // Exception cannot move to itself
            }

            if (target.organizationalUnits().contains(uoe))
            {
                // Exception invalid to
            }

            if (!source.organizationalUnits().contains(uoe))
            {
                // Exception invalid from
            }

            organizationalUnitMoved(DomainEvent.CREATE, from , to);
        }

        public void mergeOrganizationalUnit(OrganizationalUnit from, OrganizationalUnit to)
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }


        public void organizationalUnitMoved(DomainEvent event, OrganizationalUnit from, OrganizationalUnit to)
        {
            OrganizationalUnitEntity oue = (OrganizationalUnitEntity) state;
            OrganizationalUnitEntity fromEntity = (OrganizationalUnitEntity) from;
            OrganizationalUnitEntity toEntity = (OrganizationalUnitEntity) to;

            fromEntity.organizationalUnits().remove(oue);
            toEntity.organizationalUnits().add(oue);
        }

        public void organizationalUnitMerged(DomainEvent event, OrganizationalUnit from, OrganizationalUnit to)
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
