/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.rest.resource.workspace.cases.conversation;

import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.context.workspace.cases.conversation.ConversationParticipantContext;
import se.streamsource.streamflow.web.context.workspace.cases.conversation.ConversationParticipantsContext;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipants;

/**
 * JAVADOC
 */
public class ConversationParticipantsResource
   extends CommandQueryResource
         implements SubResources
{
   public ConversationParticipantsResource( )
   {
      super( ConversationParticipantsContext.class );
   }

   public void resource( String segment ) throws ContextNotFoundException
   {
      findManyAssociation( RoleMap.role( ConversationParticipants.Data.class).participants(), segment);
      subResourceContexts( ConversationParticipantContext.class);
   }
}