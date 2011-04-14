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

package se.streamsource.streamflow.web.domain.entity.user;

import se.streamsource.streamflow.web.domain.structure.user.Contactable;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.MessageReceiver;
import se.streamsource.streamflow.web.domain.structure.created.Creator;

/**
 * User representing an external email address
 */
public interface EmailUserEntity
        extends DomainEntity,

        // Interactions
        Drafts,
        Creator,
        Actor,
        MessageRecipient,

        // Structure
        ConversationParticipant,
        Describable,
        Contactable,

        // Data
        Contactable.Data,
        Describable.Data,
        MessageRecipient.Data,
        MessageReceiver.Data
{
}
