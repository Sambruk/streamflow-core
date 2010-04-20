/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.caze.conversations;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.caze.CaseResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;
import se.streamsource.streamflow.resource.conversation.ConversationDTO;

import java.util.ArrayList;
import java.util.List;

public class ConversationsModel
   implements EventListener, Refreshable, EventVisitor
{
   @Uses
   CommandQueryClient client;

   @Uses
   private ConversationModel conversationDetail;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   WeakModelMap<String, ConversationModel> conversationModels = new WeakModelMap<String, ConversationModel>(){

      @Override
      protected ConversationModel newModel( String key )
      {
         return obf.newObjectBuilder( ConversationModel.class ).use(
               client.getSubClient( key ),
               obf.newObjectBuilder( ConversationParticipantsModel.class ).
                     use(client.getSubClient( key ).getSubClient( "participants" )).newInstance(),
               obf.newObjectBuilder( MessagesModel.class ).
                     use( client.getSubClient(key).getSubClient( "messages" )).newInstance()).newInstance();
      }
   };

   TransactionList<ConversationDTO> conversations = new TransactionList<ConversationDTO>(new BasicEventList<ConversationDTO>( ));

   EventVisitorFilter eventFilter = new EventVisitorFilter( this, "addedParticipant", "removedParticipant", "createdMessage" );

   public void refresh()
   {
      try
      {
         LinksValue newConversations = client.query( "index", LinksValue.class );
         List<ConversationDTO> mutable = new ArrayList<ConversationDTO>();
         for( LinkValue link : newConversations.links().get())
         {
            mutable.add( ((ConversationDTO)link).<ConversationDTO>buildWith().prototype() );
         }
         EventListSynch.synchronize( mutable, conversations );
      } catch (Exception e)
      {
         throw new OperationException( CaseResources.could_not_refresh, e );
      }
   }

   public EventList<ConversationDTO> conversations()
   {
      return  conversations;
   }

   public void createConversation( String topic )
   {
      try
      {
         ValueBuilder<StringValue> newTopic = vbf.newValue( StringValue.class ).buildWith();
         newTopic.prototype().string().set( topic );
         client.postCommand( "create", newTopic.newInstance() );
         refresh();
         
      } catch (ResourceException e)
      {
         throw new OperationException( CaseResources.could_not_create_conversation, e );
      }
   }

    public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
      for(ConversationModel conversationModel : conversationModels)
      {
         conversationModel.notifyEvent( event );
      }
   }

   public boolean visit( DomainEvent event )
   {
      if( event.name().get().equals("createdMessage"))
      {
         for (int idx = 0; idx < conversations.size(); idx++)
         {
            ConversationDTO conversation = conversations.get( idx );
            if(conversation.id().get().equals( event.entity().get() ))
            {
               conversation.messages().set( conversation.messages().get() + 1 );
               conversations.set( idx, conversation );
            }
         }
      } else if( event.name().get().equals("addedParticipant") )
      {
         for (int idx = 0; idx < conversations.size(); idx++)
         {
            ConversationDTO conversation = conversations.get( idx );
            if(conversation.id().get().equals( event.entity().get() ) )
            {
               conversation.participants().set( conversation.participants().get() + 1 );
               conversations.set( idx, conversation );
            }
         }
      }  else if( event.name().get().equals("removedParticipant") )
      {
         for (int idx = 0; idx < conversations.size(); idx++)
         {
            ConversationDTO conversation = conversations.get( idx );
            if(conversation.id().get().equals( event.entity().get() ))
            {
               conversation.participants().set( conversation.participants().get() - 1 );
               conversations.set( idx, conversation );
            }
         }
      }

      return false;
   }
}
