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

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.dci.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.AbstractListModel;
import java.util.List;

public class OrganizationUsersModel
      extends AbstractListModel
      implements Refreshable, EventListener
{
   private CommandQueryClient client;

   private List<ListItemValue> users;

   public OrganizationUsersModel(@Uses CommandQueryClient client)
   {
      this.client = client;
      refresh();
   }


   public int getSize()
   {
      return users == null ? 0 : users.size();
   }

   public Object getElementAt( int index )
   {
      return users == null ? null : users.get( index );
   }

   public void refresh()
   {
      try
      {
         users = client.query("participatingusers", ListValue.class).items().get();
         fireContentsChanged( this, 0, users.size() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public CommandQueryClient getResource()
   {
      return client;
   }

   public void notifyEvent( DomainEvent event )
   {
      //Do nothing
   }
}
