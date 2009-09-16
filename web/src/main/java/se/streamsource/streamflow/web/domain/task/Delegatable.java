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

package se.streamsource.streamflow.web.domain.task;

import org.qi4j.api.Qi4j;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.Date;

/**
 * JAVADOC
 */
@SideEffects({Delegatable.UnreadOnStatusChangeSideEffect.class, Delegatable.UnreadOnRejectSideEffect.class})
@Mixins(Delegatable.DelegatableMixin.class)
public interface Delegatable
{
    void delegateTo(Delegatee delegatee, Delegator delegator, Delegations delegatedFrom);

    void rejectDelegation();

    interface DelegatableState
    {
        @Optional
        Association<Delegatee> delegatedTo();

        @Optional
        Association<Delegator> delegatedBy();

        @Optional
        Association<Delegations> delegatedFrom();

        @Optional
        Property<Date> delegatedOn();


        void delegatedTo(DomainEvent create, Delegatee delegatee, Delegator delegator, Delegations delegatedFrom);


        void delegationRejected(DomainEvent event);
    }

    abstract class DelegatableMixin
            implements Delegatable, DelegatableState
    {
        @This
        Ownable.OwnableState ownable;

        @Structure
        Qi4j api;

        public void delegateTo(Delegatee delegatee, Delegator delegator, Delegations delegatedFrom)
        {
            delegatedTo(DomainEvent.CREATE, delegatee, delegator, delegatedFrom);
        }

        public void delegatedTo(DomainEvent event, Delegatee delegatee, Delegator delegator, Delegations delegatedFrom)
        {
            delegatedTo().set(delegatee);
            delegatedBy().set(delegator);
            delegatedOn().set(event.on().get());
            delegatedFrom().set(delegatedFrom);
        }

        public void rejectDelegation()
        {
            if (delegatedTo().get() != null)
            {
                delegationRejected(DomainEvent.CREATE);
            }
        }

        public void delegationRejected(DomainEvent event)
        {
            delegatedTo().set(null);
            delegatedBy().set(null);
            delegatedOn().set(null);
            delegatedFrom().set(null);
        }
    }

    abstract class UnreadOnStatusChangeSideEffect
            extends SideEffectOf<TaskStatus>
            implements TaskStatus
    {
        @This
        DelegatableState state;
        @This
        TaskStatusState status;
        @This
        IsRead isRead;

        public void complete(Assignee assignee)
        {
            result.complete(assignee);

            if (state.delegatedTo() != null && !assignee.equals(state.delegatedBy().get()) && !status.status().get().equals(TaskStates.ACTIVE))
            {
                isRead.markAsUnread();
            }
        }

        public void drop(Assignee assignee)
        {
            result.drop(assignee);

            if (state.delegatedTo() != null && !assignee.equals(state.delegatedBy().get()) && !status.status().get().equals(TaskStates.ACTIVE))
            {
                isRead.markAsUnread();
            }
        }
    }


    abstract class UnreadOnRejectSideEffect
            extends SideEffectOf<Delegatable>
            implements Delegatable
    {
        @This
        IsRead read;

        public void rejectDelegation()
        {
            result.rejectDelegation();

            read.markAsUnread();
        }
    }
}