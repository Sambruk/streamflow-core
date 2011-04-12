/**
 *
 * Copyright 2009-2011 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.entity;

import org.qi4j.api.concern.*;
import org.qi4j.api.entity.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.sideeffect.*;
import se.streamsource.streamflow.infrastructure.event.domain.factory.*;
import se.streamsource.streamflow.web.domain.generic.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;

/**
 * Base Composite for all domain entities
 */
@Concerns(EventCreationConcern.class)
@Mixins({EventPropertyChangedMixin.class, CommandPropertyChangeMixin.class, EventEntityCreatedMixin.class, EventEntityAddedMixin.class, EventEntityRemovedMixin.class})
//      CommandEntityCreateMixin.class, CommandEntityRemoveMixin.class})
@SideEffects(ChangeOwnerSideEffect.class)
public interface DomainEntity
      extends EntityComposite
{
}
