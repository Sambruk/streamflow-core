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

package se.streamsource.streamflow.web.domain.project;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

/**
 * Generator for id sequences. First number is 1.
 */
@Mixins(IdGenerator.IdGeneratorMixin.class)
public interface IdGenerator
{
    /**
     * Get next id and increase internal counter.
     *
     * @return next id in the sequence
     */
    long nextId();

    /**
     * Reset the internal counter to 1.
     */
    void reset();

    interface IdGeneratorState
    {
        @UseDefaults
        Property<Long> current();
    }

    class IdGeneratorMixin
        implements IdGenerator
    {
        @This IdGeneratorState state;

        public long nextId()
        {
            long current = state.current().get();
            current++;
            state.current().set(current);
            return current;
        }

        public void reset()
        {
            state.current().set(0L);
        }
    }
}
