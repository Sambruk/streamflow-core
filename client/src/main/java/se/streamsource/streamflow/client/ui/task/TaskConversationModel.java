/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.task;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.dci.resource.CommandQueryClient;
import se.streamsource.streamflow.domain.ListValueBuilder;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.conversation.ConversationDetailDTO;
import se.streamsource.streamflow.resource.conversation.MessageDTO;
import se.streamsource.streamflow.resource.conversation.NewMessageCommand;

import java.util.ArrayList;
import java.util.Date;

public class TaskConversationModel
   implements EventListener, Refreshable
{
   @Uses
   CommandQueryClient client;

   @Uses
   TaskConversationParticipantsModel participantsModel;

   @Structure
   ValueBuilderFactory vbf;

   private ConversationDetailDTO conversationDetail;
   BasicEventList<MessageDTO> messages = new BasicEventList<MessageDTO>();

   public void refresh()
   {
      try
      {
         conversationDetail = testData();//client.query( "conversation", ConversationDetailDTO.class );
         messages.clear();
         messages.addAll(conversationDetail.messages().get() );
         participantsModel.setParticipants( conversationDetail.participants().get() );
      } catch (Exception e)
      {
         throw new OperationException( TaskResources.could_not_refresh, e );
      }
   }

   public EventList<MessageDTO> messages()
   {
      return messages;
   }

   public String getDescription()
   {
      return conversationDetail.description().get();
   }

   public TaskConversationParticipantsModel getParticipantsModel()
   {
      return participantsModel;
   }

   public void addMessage( NewMessageCommand command )
   {
      try
      {
         client.postCommand( "addmessage", command );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_add_comment, e );
      }
   }

   public CommandQueryClient getTaskConversationDetailClientResource()
   {
      return client;
   }

   public void notifyEvent( DomainEvent event )
   {

   }

   private ConversationDetailDTO testData()
   {
      ValueBuilder<MessageDTO> messageBuilder = vbf.newValueBuilder( MessageDTO.class );
      ValueBuilder<ConversationDetailDTO> cvBuilder = vbf.newValueBuilder( ConversationDetailDTO.class );
      ListValueBuilder listValueBuilder = new ListValueBuilder(vbf);

      MessageDTO message = messageBuilder.prototype();
      message.body().set( "Tables are defined with the table tag. A table is divided into rows (with the tr tag), and each row is divided into data cells (with the td tag). The letters td stands for table data, which is the content of a data cell. A data cell can contain text, images, lists, paragraphs, forms, horizontal rules, tables, etc. " );
      message.sender().set( EntityReference.parseEntityReference( "administrator" ));
      message.createdOn().set( new Date() );

      ArrayList<MessageDTO> msgs = new ArrayList<MessageDTO>(5);
      msgs.add( messageBuilder.newInstance() );

      message.body().set("This is another new Message" );
      msgs.add( messageBuilder.newInstance() );

      listValueBuilder.addListItem( "someuser1", EntityReference.parseEntityReference( "someuser1" ));
      listValueBuilder.addListItem( "someuser2", EntityReference.parseEntityReference( "someuser2" ));
      listValueBuilder.addListItem( "someuser3", EntityReference.parseEntityReference( "someuser3" ));

      ConversationDetailDTO cv = cvBuilder.prototype();
      cv.creationDate().set( new Date() );
      cv.creator().set( "administrator" );
      cv.description().set( "Some konversation" );
      cv.messages().set( msgs );

      cv.participants().set( listValueBuilder.newList() );

      return cvBuilder.newInstance();
   }
}
