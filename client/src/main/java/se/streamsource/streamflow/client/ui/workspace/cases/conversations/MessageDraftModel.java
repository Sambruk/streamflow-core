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
package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.restlet.data.Form;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

/**
 *
 */
public class MessageDraftModel
      implements Refreshable, TransactionListener
{
   @Uses
   CommandQueryClient client;

   @Structure
   Module module;

   StringValue message;

   public void refresh()
   {
      message = client.query( "index", StringValue.class );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.onEntities( client.getReference().getParentRef().getLastSegment() ), transactions ))
         refresh();
   }

   public AttachmentsModel newMessageDraftAttachmentsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(AttachmentsModel.class).use(client.getSubClient( "attachments" )).newInstance();
   }

   public void changeMessage( String message )
   {
      Form form = new Form();
      form.add( "message", message );
      client.postCommand( "changemessage", form.getWebRepresentation() );
   }

   public StringValue getDraftMessage()
   {
      return message;
   }
}