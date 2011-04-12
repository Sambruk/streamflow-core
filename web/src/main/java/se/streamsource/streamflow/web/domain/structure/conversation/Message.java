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

package se.streamsource.streamflow.web.domain.structure.conversation;

import org.qi4j.api.entity.association.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.property.*;
import se.streamsource.streamflow.util.*;

import java.text.*;
import java.util.*;

/**
 * JAVADOC
 */
@Mixins(Message.Mixin.class)
public interface Message
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

   class Mixin
      implements Message
   {
      @This
      Data data;

      public String translateBody(Map<String, String> translations)
      {
         String body = data.body().get();

         if( body.startsWith("{") && body.endsWith("}") )
         {
            String[] tokens = body.substring(1,body.length()-1).split( "," );
            String key = tokens[0];
            String translation = translations.get(key);
            if (Strings.empty(translation))
               return "";
            String[] args = new String[tokens.length-1];
            System.arraycopy(tokens, 1, args, 0, args.length);
            body = new MessageFormat( translation ).format( args );
            return body;
         } else
            return body;
      }
   }
}
