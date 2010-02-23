/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.administration.tasktypes;

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
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

/**
 * Management of selected tasktypes on a project
 */
public class SelectedTaskTypesModel
      implements EventListener, Refreshable
{
   @Uses
   CommandQueryClient client;

   BasicEventList<LinkValue> eventList = new BasicEventList<LinkValue>();

   @Structure
   ValueBuilderFactory vbf;

   private LinksValue list;

   public EventList<LinkValue> getTaskTypeList()
   {
      return eventList;
   }

   public void refresh()
   {
      try
      {
         // Get tasktype list
         LinksValue newList = client.query( "index", LinksValue.class );

         if (list == null || !newList.equals( list ))
         {
            eventList.clear();
            eventList.addAll( newList.links().get() );
            list = newList;
         }

      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh_list_of_tasktypes, e );
      }
   }

   public EventList<LinkValue> getPossibleTaskTypes()
   {
      try
      {
         BasicEventList<LinkValue> possibleTaskTypes = new BasicEventList<LinkValue>();
         possibleTaskTypes.addAll( client.query( "possibletasktypes", LinksValue.class ).links().get() );
         return possibleTaskTypes;
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public void addTaskType( LinkValue identity )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( EntityReference.parseEntityReference( identity.id().get()) );
         client.postCommand( "addtasktype", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_add_tasktype, e );
      }
   }

   public void removeTaskType( EntityReference identity )
   {
      try
      {
         client.getSubClient( identity.identity() ).delete();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_tasktype, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
   }
}