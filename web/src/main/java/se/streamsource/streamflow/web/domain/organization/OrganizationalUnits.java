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

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.streamflow.domain.CreatedEvent;
import se.streamsource.streamflow.infrastructure.event.Command;
import se.streamsource.streamflow.infrastructure.event.Event;
import se.streamsource.streamflow.infrastructure.event.EventBuilderFactory;

/**
 * JAVADOC
 */
@Mixins(OrganizationalUnits.OrganizationsMixin.class)
public interface OrganizationalUnits
{
    @Command
    OrganizationalUnit createOrganizationalUnit(@MaxLength(50) String name);

    @Event
    OrganizationalUnit organizationalUnitCreated(CreatedEvent event);

    interface OrganizationalUnitsState
    {
        @Aggregated
        ManyAssociation<OrganizationalUnit> organizationalUnits();

    }

    class OrganizationsMixin
            implements OrganizationalUnits
    {
        @Service
        EventBuilderFactory ebf;

        @Service
        IdentityGenerator idGenerator;

        @This
        OrganizationalUnit.OrganizationalUnitState ouState;

        @This
        OrganizationalUnitsState state;

        @Structure
        UnitOfWorkFactory uowf;

        public OrganizationalUnit createOrganizationalUnit(String name)
        {
            ValueBuilder<CreatedEvent> valueBuilder = ebf.buildEvent(CreatedEvent.class);
            valueBuilder.prototype().createdId().set(idGenerator.generate(OrganizationalUnitEntity.class));
            OrganizationalUnitEntity ou = organizationalUnitCreated(valueBuilder.newInstance());
            ou.describe(name);
            return ou;
        }

        public OrganizationalUnitEntity organizationalUnitCreated(CreatedEvent event)
        {
            EntityBuilder<OrganizationalUnitEntity> ouBuilder = uowf.currentUnitOfWork().newEntityBuilder(OrganizationalUnitEntity.class, event.createdId().get());
            ouBuilder.prototype().organization().set(ouState.organization().get());
            OrganizationalUnitEntity ou = ouBuilder.newInstance();
            state.organizationalUnits().add(state.organizationalUnits().count(), ou);
            return ou;
        }
    }
}
