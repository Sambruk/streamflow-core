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

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;

/**
 * This class is a intermediate class for creating messages that can have attachments added by Streamflow.
 * This is necessary due to the fact that createdMessage event triggers the distribution av email through the
 * event monitoring system. Thus in mind we need to create a draft message first that can be used to attache files
 * and will call createMessage when ready.
 */
@Mixins( MessageDraft.Mixin.class )
public interface MessageDraft
{
   void changeDraftMessage( @Optional String message );

   void discardDraftMessage();

   String getDraftMessage();

   interface Data
   {
      @Optional
      Property<String> draftmessage();

      void changedDraftMessage( @Optional DomainEvent event, @Optional String message );

      void discardedDraftMessage( @Optional DomainEvent event );
   }

   abstract class Mixin
      implements MessageDraft, Data
   {
      @This
      Data data;

      @This
      Attachments attachments;

      public void changeDraftMessage( @Optional String message )
      {
         if( !Strings.propertyEquals( data.draftmessage(), message ))
         {
            changedDraftMessage( null, message );
         }
      }

      public void changedDraftMessage( @Optional DomainEvent event, String message )
      {
         data.draftmessage().set( message );
      }

      public void discardDraftMessage()
      {
         discardedDraftMessage( null );
         for(Attachment attachment : ((Attachments.Data)attachments).attachments() )
         {
            attachments.removeAttachment( attachment );
         }
      }

      public void discardedDraftMessage( @Optional DomainEvent event )
      {
         data.draftmessage().set( null );
      }

      public String getDraftMessage()
      {
         return data.draftmessage().get();
      }

   }
}
