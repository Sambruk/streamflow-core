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

package se.streamsource.streamflow.client.ui.administration.organization;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.application.LinksValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitorFilter;

import javax.swing.AbstractListModel;
import java.util.List;
import java.util.logging.Logger;

public class OrganizationsModel
      extends AbstractListModel
      implements EventListener, EventVisitor
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

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

   private List<LinkValue> organizations;

   private CommandQueryClient client;

   public OrganizationsModel(@Uses CommandQueryClient client)
   {
      this.client = client;
      this.refresh();
   }

   public int getSize()
   {
      return organizations == null ? 0 : organizations.size();
   }

   public Object getElementAt( int index )
   {
      return organizations == null ? null : organizations.get( index );
   }

   public void refresh()
   {
      try
      {
         organizations = client.query("organizations", LinksValue.class).links().get();
         fireContentsChanged( this, 0, organizations.size() );
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
      Logger.getLogger( "administration" ).info( "Refresh organizations" );
      refresh();
      return false;
   }
}
