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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

import javax.swing.AbstractListModel;
import java.util.List;

/**
 * Model for the list of currently selected labels of a task
 */
public class TaskLabelsModel
      extends AbstractListModel
      implements EventListener
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   List<ListItemValue> labels;

   public void setLabels( ListValue labels )
   {
      this.labels = labels.items().get();

      fireContentsChanged( this, 0, this.labels.size() );
   }

   public int getSize()
   {
      return labels == null ? 0 : labels.size();
   }

   public ListItemValue getElementAt( int index )
   {
      return labels == null ? null : labels.get( index );
   }

   public void addLabel( EntityReference addLabel )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( addLabel );
         client.postCommand( "addlabel", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_add_label, e );
      }
   }

   public void removeLabel( EntityReference removeLabel )
   {
      int idx = -1;
      for (int i = 0; i < labels.size(); i++)
      {
         ListItemValue listItemValue = labels.get( i );
         if (listItemValue.entity().get().equals( removeLabel ))
            idx = i;
      }

      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( removeLabel );
         client.putCommand( "removelabel", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( TaskResources.could_not_remove_label, e );
      }

      if (idx != -1)
         fireIntervalRemoved( this, idx, idx );
   }

   public void notifyEvent( DomainEvent event )
   {
   }
}
