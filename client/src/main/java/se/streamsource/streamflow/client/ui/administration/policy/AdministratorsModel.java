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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.StringDTO;

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

   private BasicEventList<ListItemValue> administrators = new BasicEventList<ListItemValue>( );

   public EventList<ListItemValue> getAdministrators()
   {
      return administrators;
   }

   public void addAdministrator( String description )
   {
      try
      {
         ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
         builder.prototype().string().set( description );
         client.postCommand( "addadministrator", builder.newInstance() );
         refresh();

      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_add_administrator, e );
      }

   }

   public void removeAdministrator( String id )
   {
      try
      {
         client.getSubClient( id ).deleteCommand();
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
         ListValue administratorsList = client.query( "administrators", ListValue.class );
         administrators.clear();
         administrators.addAll( administratorsList.items().get() );
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