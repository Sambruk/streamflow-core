/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.entity;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffects;

import se.streamsource.streamflow.infrastructure.event.domain.factory.EventCreationConcern;
import se.streamsource.streamflow.web.domain.generic.CommandPropertyChangeMixin;
import se.streamsource.streamflow.web.domain.generic.EventEntityAddedMixin;
import se.streamsource.streamflow.web.domain.generic.EventEntityCreatedMixin;
import se.streamsource.streamflow.web.domain.generic.EventEntityRemovedMixin;
import se.streamsource.streamflow.web.domain.generic.EventPropertyChangedMixin;
import se.streamsource.streamflow.web.domain.interaction.gtd.ChangeOwnerSideEffect;

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
