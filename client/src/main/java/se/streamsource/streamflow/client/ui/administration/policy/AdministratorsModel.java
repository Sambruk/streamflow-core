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

package se.streamsource.streamflow.client.ui.administration.policy;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

/**
 * JAVADOC
 */
public class AdministratorsModel
      implements Refreshable, EventListener
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   CommandQueryClient client;

   private EventList<LinkValue> administrators = new BasicEventList<LinkValue>( );

   public EventList<LinkValue> getAdministrators()
   {
      return administrators;
   }

   public void addAdministrator( String description )
   {
      try
      {
         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         builder.prototype().entity().set( EntityReference.parseEntityReference(description ));
         client.postCommand( "addadministrator", builder.newInstance() );
         refresh();

      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_add_administrator, e );
      }

   }

   public void removeAdministrator( String href )
   {
      try
      {
         client.getClient( href ).delete();
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_administrator, e );
      }
   }

   public void refresh()
   {
      try
      {
         LinksValue administratorsList = client.query( "administrators", LinksValue.class );
         EventListSynch.synchronize( administratorsList.links().get(), administrators );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
   }

   public CommandQueryClient getFilterResource()
   {
      return client;
   }
}