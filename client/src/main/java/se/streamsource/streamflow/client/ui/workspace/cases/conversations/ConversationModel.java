/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.restlet.client.CommandQueryClient;

/**
 * TODO
 */
public class ConversationModel
{
   @Uses
   CommandQueryClient client;

   @Structure
   Module module;

   public ConversationParticipantsModel newParticipantsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(ConversationParticipantsModel.class).use(client.getSubClient( "participants" )).newInstance();
   }

   public MessagesModel newMessagesModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(MessagesModel.class).use(client.getSubClient( "messages" )).newInstance();
   }
}
