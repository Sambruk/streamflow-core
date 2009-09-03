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

package se.streamsource.streamflow.domain.roles;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

/**
 * Generic interface for removing objects. They are not
 * physically removed, but are instead only marked as removed.
 * All state still exists and can be queried.
 */
@Mixins(Removable.RemovableMixin.class)
public interface Removable
{
    void remove();

    interface RemovableState
    {
        @UseDefaults
        Property<Boolean> removed();
    }

    class RemovableMixin
            implements Removable
    {
        @This
        RemovableState state;

        public void remove()
        {
            state.removed().set(true);
        }
    }
}
