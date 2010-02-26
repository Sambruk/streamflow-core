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

package se.streamsource.streamflow.client.ui.task.conversations;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.task.TaskResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * Model for the list of currently selected participants of a conversation
 */
public class AllParticipantsModel
      implements EventListener
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   BasicEventList<ListItemValue> participants = new BasicEventList<ListItemValue>( );

   public EventList<ListItemValue> getParticipants()
   {
      return participants;
   }

   public void setParticipants( ListValue participants )
   {
      this.participants.clear();
      this.participants.addAll( participants.items().get() );
   }
   
   public void addParticipant( EntityReference addParticipant )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( addParticipant );
         client.putCommand( "addparticipant", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_add_participant, e );
      }
   }

   public void removeParticipant( EntityReference removeParticipant )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( removeParticipant );
         client.getSubClient( removeParticipant.identity() ).delete();
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_remove_participant, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
   }
}
