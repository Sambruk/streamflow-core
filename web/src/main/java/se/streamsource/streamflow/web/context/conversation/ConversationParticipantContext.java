/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.context.conversation;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipants;

/**
 * JAVADOC
 */
@Mixins(ConversationParticipantContext.Mixin.class)
public interface ConversationParticipantContext
   extends DeleteContext, Context
{
   abstract class Mixin
      extends ContextMixin
      implements ConversationParticipantContext
   {
      public void delete()
      {
         ConversationParticipant participant = roleMap.get( ConversationParticipant.class );
         ConversationParticipants participants = roleMap.get( ConversationParticipants.class);
         participants.removeParticipant( participant );
      }

   }
}