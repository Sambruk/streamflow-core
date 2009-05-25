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

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.organization.DuplicateDescriptionException;

/**
 * JAVADOC
 */
@Mixins(Groups.GroupsMixin.class)
public interface Groups
{
    void addGroup(Group group) throws DuplicateDescriptionException;

    void removeGroup(Group group);

    interface GroupsState
    {
        @Aggregated
        ManyAssociation<Group> groups();
    }

    class GroupsMixin
            implements Groups
    {
        @This
        GroupsState state;

        public void addGroup(Group group) throws DuplicateDescriptionException
        {
            String groupName = group.getDescription();
            for (Group agroup : state.groups())
            {
                if (agroup.hasDescription(groupName))
                {
                    throw new DuplicateDescriptionException();
                }
            }

            state.groups().add(state.groups().count(), group);
        }

        // TODO throw NoSuchGroupException
        public void removeGroup(Group group)
        {
            state.groups().remove(group);
        }
    }


}
