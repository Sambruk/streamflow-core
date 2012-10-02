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
package se.streamsource.streamflow.web.domain.structure.conversation;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.caze.Case;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JAVADOC
 */
@Mixins(Message.Mixin.class)
public interface Message
   extends Attachments, Removable
{
   String translateBody(Map<String, String> translations);

   interface Data
   {
      @Immutable
      Association<Conversation> conversation();

      @Immutable
      Association<ConversationParticipant> sender();

      @Immutable
      Property<Date> createdOn();

      @Immutable
      Property<String> body();
   }

   abstract class Mixin
      implements Message
   {
      @This
      Data data;

      public String translateBody(Map<String, String> translations)
      {
         Map<String,String> variables = new HashMap<String,String>();
         
         // Bind standard variables
         ConversationOwner owner = data.conversation().get().conversationOwner().get();
         if (owner instanceof Case)
         {
            variables.put("caseid", ((CaseId.Data)owner).caseId().get());
         }
         variables.put("subject", ((Describable.Data)data.conversation().get()).description().get());

         return Translator.translate( data.body().get(), translations, variables );
         
      }
   }
}
