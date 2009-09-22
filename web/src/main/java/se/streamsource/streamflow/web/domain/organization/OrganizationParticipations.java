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

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * List of organizations a participant is a member of.
 */
@Mixins(OrganizationParticipations.OrganizationParticipationsMixin.class)
public interface OrganizationParticipations
{
    void join(Organization org);

    void leave(Organization ou);

    interface OrganizationParticipationsState
    {
        ManyAssociation<Organization> organizations();

        void organizationJoined(DomainEvent event, Organization org);
        void organizationLeft(DomainEvent event, Organization org);
    }

    abstract class OrganizationParticipationsMixin
            implements OrganizationParticipations, OrganizationParticipationsState
    {
        @This
        OrganizationParticipationsState state;

        public void join(Organization ou)
        {
            if (!state.organizations().contains(ou))
            {
                organizationJoined(DomainEvent.CREATE, ou);
            }
        }

        public void leave(Organization ou)
        {
            if (state.organizations().contains(ou))
            {
                organizationLeft(DomainEvent.CREATE, ou);
            }
        }

        public void organizationJoined(DomainEvent event, Organization org)
        {
            state.organizations().add(org);
        }

        public void organizationLeft(DomainEvent event, Organization org)
        {
            state.organizations().remove(org);
        }
    }
}
