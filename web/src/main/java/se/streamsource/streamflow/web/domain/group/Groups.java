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
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.organization.DuplicateDescriptionException;

/**
 * JAVADOC
 */
@Mixins(Groups.GroupsMixin.class)
public interface Groups
{
    Group newGroup(String name) throws DuplicateDescriptionException;

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

        @Structure
        UnitOfWorkFactory uowf;

        public Group newGroup(String name) throws DuplicateDescriptionException
        {
            for (Group agroup : state.groups())
            {
                if (agroup.hasDescription(name))
                {
                    throw new DuplicateDescriptionException();
                }
            }

            // Create group
            EntityBuilder<GroupEntity> groupBuilder = uowf.currentUnitOfWork().newEntityBuilder(GroupEntity.class);
            groupBuilder.prototype().describe(name);
            Group group = groupBuilder.newInstance();

            state.groups().add(state.groups().count(), group);

            return group;
        }

        public void removeGroup(Group group)
        {
            if (state.groups().remove(group))
            {
                uowf.currentUnitOfWork().remove(group);
            }
        }
    }


}
