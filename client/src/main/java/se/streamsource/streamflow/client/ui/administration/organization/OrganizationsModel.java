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

package se.streamsource.streamflow.client.ui.administration.organization;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizationsModel
      implements EventListener, EventVisitor
{
   final Logger logger = LoggerFactory.getLogger( "administration" );
   @Structure
   ObjectBuilderFactory obf;

   private EventVisitorFilter eventFilter = new EventVisitorFilter( this, "createdOrganization", "createdUser" );

   WeakModelMap<String, LinksListModel> organizationUsersModels = new WeakModelMap<String, LinksListModel>()
   {
      @Override
      protected LinksListModel newModel( String key )
      {
         return obf.newObjectBuilder( LinksListModel.class )
               .use( client.getSubClient(key).getSubClient( "users" ), "users" ).newInstance();
      }
   };

   private EventList<LinkValue> organizations = new BasicEventList<LinkValue>();

   private CommandQueryClient client;

   public OrganizationsModel(@Uses CommandQueryClient client)
   {
      this.client = client;
      this.refresh();
   }


   public EventList<LinkValue> getEventList()
   {
      return organizations;
   }

   public void refresh()
   {
      try
      {
         List<LinkValue> orgs = client.query("index", LinksValue.class).links().get();
         EventListSynch.synchronize( orgs, organizations );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }


   public LinksListModel getOrganizationUsersModel( String id )
   {
      return organizationUsersModels.get( id );
   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );

      for (LinksListModel model : organizationUsersModels)
      {
         model.notifyEvent( event );
      }
   }

   public boolean visit( DomainEvent event )
   {
      logger.info( "Refresh organizations" );
      refresh();
      return false;
   }
}
