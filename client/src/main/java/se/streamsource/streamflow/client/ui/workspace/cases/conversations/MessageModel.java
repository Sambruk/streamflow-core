package se.streamsource.streamflow.client.ui.workspace.cases.conversations;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO;
import se.streamsource.streamflow.client.ui.workspace.cases.attachments.AttachmentsModel;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

/**
 *
 */
public class MessageModel
      implements Refreshable, TransactionListener
{
   @Uses
   CommandQueryClient client;

   @Structure
   Module module;

   MessageDTO message;

   public void refresh()
   {
      message = client.query( "index", MessageDTO.class );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.onEntities( client.getReference().getParentRef().getLastSegment() ), transactions ))
         refresh();
   }

   public AttachmentsModel newMessageAttachmentsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(AttachmentsModel.class).use(client.getSubClient( "attachments" )).newInstance();
   }

   public MessageDTO getMessageDTO()
   {
      return message;
   }
}