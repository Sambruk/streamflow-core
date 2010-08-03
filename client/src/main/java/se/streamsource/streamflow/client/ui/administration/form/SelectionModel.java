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

package se.streamsource.streamflow.client.ui.administration.form;

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
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;

/**
 * Management of selected entities
 */
public class SelectionModel
      implements EventListener, Refreshable
{
   @Uses
   CommandQueryClient client;

   BasicEventList<LinkValue> forms = new BasicEventList<LinkValue>();

   @Structure
   ValueBuilderFactory vbf;

   private String index;
   private String possible;

   public SelectionModel(String index, String possible, String add)
   {
      this.index = index;
      this.possible = possible;
      String add1 = add;
   }

   public EventList<LinkValue> getEventList()
   {
      return forms;
   }

   public void refresh()
   {
      try
      {
         // Get list
         LinksValue newList = client.query( index, LinksValue.class );
         EventListSynch.synchronize( newList.links().get(), forms );

      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public EventList<LinkValue> getPossible()
   {
      try
      {
         BasicEventList<LinkValue> possibleLinks = new BasicEventList<LinkValue>();
         possibleLinks.addAll( client.query( possible, LinksValue.class ).links().get() );
         return possibleLinks;
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public void add( LinkValue link )
   {
      try
      {
         client.postLink( link );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_add, e );
      }
   }

   public void remove( LinkValue linkValue )
   {
      try
      {
         client.getClient( linkValue ).delete();

         forms.remove( linkValue );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
   }
}