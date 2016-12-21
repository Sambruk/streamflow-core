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
package se.streamsource.streamflow.web.domain.entity.conversation;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.This;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Unread;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;

/**
 * JAVADOC
 */
@Concerns( MessageEntity.RemovableConcern.class)
public interface MessageEntity
   extends Message,
      Message.Data,
      Attachments.Data,
      Unread.Data,
      Removable.Data,
      DomainEntity
{
   abstract class RemovableConcern
      extends ConcernOf<Removable>
      implements Removable
   {
      @This
      Attachments attachments;
      @This
      Attachments.Data attachementsData;


      public void deleteEntity()
      {
         for( Attachment attachment : attachementsData.attachments().toList() )
         {
            attachments.removeAttachment( attachment );
         }

         next.deleteEntity();
      }
   }
}
