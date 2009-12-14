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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.organizations.policy.AdministratorsClientResource;
import se.streamsource.streamflow.client.ui.UsersAndGroupsFilter;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.StringDTO;

import javax.swing.AbstractListModel;
import java.util.List;

/**
 * JAVADOC
 */
public class AdministratorsModel
      extends AbstractListModel
      implements Refreshable, EventListener
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   private AdministratorsClientResource administrators;

   private List<ListItemValue> list;

   public int getSize()
   {
      return list == null ? 0 : list.size();
   }

   public Object getElementAt( int index )
   {
      return list == null ? null : list.get( index );
   }

   public void addAdministrator( String description )
   {
      try
      {
         ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
         builder.prototype().string().set( description );
         administrators.addAdministrator( builder.newInstance() );
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
         administrators.role( id ).deleteCommand();
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
         list = administrators.administrators().items().get();
         fireContentsChanged( this, 0, list.size() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public void notifyEvent( DomainEvent event )
   {
   }

   public UsersAndGroupsFilter getFilterResource()
   {
      return administrators;
   }
}