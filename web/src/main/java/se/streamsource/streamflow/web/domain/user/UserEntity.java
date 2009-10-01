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

package se.streamsource.streamflow.web.domain.user;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.injection.scope.This;
import se.streamsource.streamflow.domain.contact.Contactable;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.web.domain.DomainEntity;
import se.streamsource.streamflow.web.domain.comment.Commenter;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.label.Labels;
import se.streamsource.streamflow.web.domain.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.task.*;

/**
 * JAVADOC
 */
@Concerns(UserEntity.LifecycleConcern.class)
public interface UserEntity
        extends DomainEntity,
        Lifecycle,

        // Roles
        Assignee,
        Assignments,
        Commenter,
        Contactable,
        Delegatee,
        Delegator,
        Delegations,
        Describable,
        OrganizationParticipations,
        Owner,
        Participant,
        Inbox,
        InboxQueries,
        WaitingFor,
        Labels,
        User,

        // State
        Contactable.ContactableState,
        OrganizationParticipations.OrganizationParticipationsState,
        Describable.DescribableState,
        Labels.LabelsState,
        Participant.ParticipantState,
        User.UserState
{
    public static final String ADMINISTRATOR_USERNAME = "administrator";

    class LifecycleConcern
            extends ConcernOf<Lifecycle>
            implements Lifecycle
    {
        @This
        Identity identity;
        @This
        Describable.DescribableState state;

        public void create() throws LifecycleException
        {
            state.description().set(identity.identity().get());

            next.create();
        }

        public void remove() throws LifecycleException
        {
            next.remove();
        }
    }
}
