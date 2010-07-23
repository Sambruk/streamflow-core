/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.ui.caze;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

/**
 * Model for the list of currently selected labels of a case
 */
public class CaseLabelsModel
      implements EventListener
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   BasicEventList<LinkValue> labels = new BasicEventList<LinkValue>( );

   public EventList<LinkValue> getLabels()
   {
      return labels;
   }

   public void setLabels( LinksValue labels )
   {
      EventListSynch.synchronize( labels.links().get(), this.labels );
   }
   
   public void addLabel( EntityReference addLabel )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( addLabel );
         client.putCommand( "addlabel", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( CaseResources.could_not_add_label, e );
      }
   }

   public void removeLabel( EntityReference removeLabel )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( removeLabel );
         client.getSubClient( removeLabel.identity() ).delete();
      } catch (ResourceException e)
      {
         throw new OperationException( CaseResources.could_not_remove_label, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
   }
}
