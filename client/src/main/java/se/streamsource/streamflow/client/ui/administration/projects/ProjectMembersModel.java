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

package se.streamsource.streamflow.client.ui.administration.projects;

import ca.odell.glazedlists.BasicEventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import java.util.Set;

/**
 * JAVADOC
 */
public class ProjectMembersModel
      implements Refreshable, EventListener

{
   @Uses
   CommandQueryClient client;

   @Uses
   OrganizationalUnitAdministrationModel ouAdminModel;

   @Structure
   ObjectBuilderFactory obf;

   private BasicEventList<ListItemValue> members = new BasicEventList<ListItemValue>( );

   public BasicEventList<ListItemValue> getMembers()
   {
      return members;
   }

   public void refresh()
   {
      try
      {
         ListValue membersList = client.query( "members", ListValue.class);
         members.clear();
         members.addAll( membersList.items().get() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh_list_of_members, e );
      }
   }

   public void addMembers( Set<String> newMembers )
   {
      try
      {
         for (String value : newMembers)
         {
            client.getSubClient( value ).create();
         }
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_add_members, e );
      }
   }

   public void removeMember( int index )
   {
      try
      {
         String id = members.get( index ).entity().get().identity();

         client.getSubClient( id ).deleteCommand();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_member, e );
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