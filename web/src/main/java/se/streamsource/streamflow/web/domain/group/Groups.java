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

package se.streamsource.streamflow.web.domain.group;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Concerns(Groups.DescribeCreatedGroupConcern.class)
@Mixins(Groups.GroupsMixin.class)
public interface Groups
{
    GroupEntity createGroup(String name);

    void addGroup(GroupEntity group);

    boolean removeGroup(Group group);

    void mergeGroups(Groups groups);

    interface GroupsState
    {
        @Aggregated
        ManyAssociation<Group> groups();

        GroupEntity groupCreated(DomainEvent event, String id);
        void groupAdded(DomainEvent event, GroupEntity group);
        void groupRemoved(DomainEvent event, Group group);
    }

    abstract class GroupsMixin
        implements Groups, GroupsState
    {
        public void mergeGroups(Groups groups)
        {

            while (this.groups().count() >0)
            {
                Group group = this.groups().get(0);
                removeGroup(group);
                groups.addGroup((GroupEntity) group);
            }

        }

        public void addGroup(GroupEntity group)
        {
            if (groups().contains(group))
            {
                return;
            }
            groupAdded(DomainEvent.CREATE, group);
        }
    }

    abstract class DescribeCreatedGroupConcern
        extends ConcernOf<Groups>
        implements Groups
    {
        public GroupEntity createGroup(String name)
        {
            GroupEntity group = next.createGroup(name);
            group.changeDescription(name);
            return group;
        }
    }

}
