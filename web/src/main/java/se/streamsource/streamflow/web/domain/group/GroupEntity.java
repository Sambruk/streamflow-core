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
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import se.streamsource.streamflow.domain.roles.Describable;

/**
 * JAVADOC
 */
@Concerns(GroupEntity.GroupLifeycleConcern.class)
public interface GroupEntity
        extends Group,
        Describable.DescribableState,
        Participants.ParticipantsState,
        EntityComposite
{
    class GroupLifeycleConcern
            extends ConcernOf<Lifecycle>
            implements Lifecycle
    {
        public void create() throws LifecycleException
        {
            next.create();
        }

        public void remove() throws LifecycleException
        {
            // TODO Remove from other groups
            // TODO Remove from project role assignments

            next.remove();
        }
    }
}
