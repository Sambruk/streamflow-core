package se.streamsource.streamflow.web.context.workspace.cases.conversation;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

/**
 *
 */
public class MessageContext
   implements IndexContext<MessageDTO>
{
   @Structure
   Module module;

   public MessageDTO index()
   {
      Message.Data messageData = RoleMap.current().get( Message.Data.class );
      ValueBuilder<MessageDTO> builder = module.valueBuilderFactory().newValueBuilder( MessageDTO.class );
      Contactable contact = module.unitOfWorkFactory().currentUnitOfWork().get( Contactable.class, EntityReference.getEntityReference( messageData.sender().get() ).identity() );
      String sender = contact.getContact().name().get();
      builder.prototype().sender().set( !"".equals( sender )
            ? sender
            : EntityReference.getEntityReference( messageData.sender().get() ).identity() );
      builder.prototype().createdOn().set( messageData.createdOn().get() );
      builder.prototype().id().set( ((Identity)messageData).identity().get() );
      builder.prototype().href().set( builder.prototype().id().get() );
      builder.prototype().text().set( messageData.body().get() );
      builder.prototype().hasAttachments().set( ((Message)messageData).hasAttachments() );

      return builder.newInstance();
   }
}
