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
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.conversation.ConversationDTO;
import se.streamsource.streamflow.resource.conversation.ConversationsDTO;
import se.streamsource.streamflow.resource.conversation.MessageDTO;
import se.streamsource.streamflow.resource.conversation.NewConversationCommand;
import se.streamsource.streamflow.resource.user.UserEntityDTO;

import java.util.ArrayList;
import java.util.Date;

public class TaskConversationsModel
   implements EventListener, Refreshable
{
   @Uses
   CommandQueryClient client;

   @Uses
   private TaskConversationModel conversationDetail;

   @Structure
   ValueBuilderFactory vbf;

   BasicEventList<ConversationDTO> conversations = new BasicEventList<ConversationDTO>();

   public void refresh()
   {
      try
      {
         ConversationsDTO newConversations = testData();//client.query( "conversations", ConversationsDTO.class );
         conversations.clear();
         conversations.addAll(newConversations.conversations().get() );
      } catch (Exception e)
      {
         throw new OperationException( TaskResources.could_not_refresh, e );
      }
   }

   public EventList<ConversationDTO> conversations()
   {
      return  conversations;
   }

   public void addConversation( NewConversationCommand command )
   {
      try
      {
         client.postCommand( "addconversation", command );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_add_comment, e );
      }
   }

   public TaskConversationModel detail()
   {
      return conversationDetail;
   }
   
   public void notifyEvent( DomainEvent event )
   {

   }

   private ConversationsDTO testData()
   {
      ValueBuilder<MessageDTO> messageBuilder = vbf.newValueBuilder( MessageDTO.class );
      ValueBuilder<UserEntityDTO> usrBuilder = vbf.newValueBuilder( UserEntityDTO.class );
      ValueBuilder<ConversationDTO> cvBuilder = vbf.newValueBuilder( ConversationDTO.class );
      ValueBuilder<ConversationsDTO> cvsBuilder = vbf.newValueBuilder( ConversationsDTO.class );

      MessageDTO message = messageBuilder.prototype();
      message.body().set( "This is a new Message" );
      message.sender().set( EntityReference.parseEntityReference( "administrator" ));
      message.createdOn().set( new Date() );

      ArrayList<MessageDTO> msgs = new ArrayList<MessageDTO>(5);
      msgs.add( messageBuilder.newInstance() );

      UserEntityDTO user = usrBuilder.prototype();
      user.disabled().set( new Boolean(false) );
      user.entity().set( EntityReference.parseEntityReference( "administrator" ));
      user.username().set( "administrator" );

      ArrayList<UserEntityDTO> participants = new ArrayList<UserEntityDTO>(5);
      participants.add( usrBuilder.newInstance() );

      ConversationDTO cv = cvBuilder.prototype();
      cv.id().set(EntityReference.parseEntityReference( "99" ));
      cv.creationDate().set( new Date() );
      cv.creator().set( EntityReference.parseEntityReference( "administrator" ));
      cv.description().set( "Some konversation" );
      cv.messages().set( msgs.size() );

      cv.participants().set( participants.size() );

      ArrayList<ConversationDTO> cvList = new ArrayList<ConversationDTO>(5);
      cvList.add( cvBuilder.newInstance() );

      for(int i = 0; i< 50; i++)
      {
         cv.description().set( "Some serious conversation " + i );
         cv.id().set(EntityReference.parseEntityReference( "" + i ));
         cvList.add( cvBuilder.newInstance() );
      }
      cvsBuilder.prototype().conversations().set( cvList );

      return cvsBuilder.newInstance();
   }
}
