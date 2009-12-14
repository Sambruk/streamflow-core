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

package se.streamsource.streamflow.client.ui.administration.groups;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.organizations.groups.GroupsClientResource;
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
public class GroupsModel
      extends AbstractListModel
      implements Refreshable, EventListener
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   WeakModelMap<String, GroupModel> groupModels = new WeakModelMap<String, GroupModel>()
   {
      @Override
      protected GroupModel newModel( String key )
      {
         return obf.newObjectBuilder( GroupModel.class ).use( groupsResource.group( key ) ).newInstance();
      }
   };

   @Uses
   private GroupsClientResource groupsResource;

   private List<ListItemValue> groups;

   public void newGroup( String description )
   {
      try
      {
         ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
         builder.prototype().string().set( description );
         groupsResource.createGroup( builder.newInstance() );
         refresh();
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_create_group_name_already_exists, e );
         }
         throw new OperationException( AdministrationResources.could_not_create_group, e );
      }
   }

   public void removeGroup( String id )
   {
      try
      {
         groupsResource.group( id ).deleteCommand();
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_group, e );
      }
   }

   public int getSize()
   {
      return groups == null ? 0 : groups.size();
   }

   public Object getElementAt( int index )
   {
      return groups == null ? null : groups.get( index );
   }

   public void refresh()
   {
      try
      {
         // Get label list
         groups = groupsResource.groups().items().get();

         fireContentsChanged( this, 0, groups.size() );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }


   public GroupModel getGroupModel( String id )
   {
      return groupModels.get( id );
   }

   public void changeDescription( int selectedIndex, String newName )
   {
      ValueBuilder<StringDTO> builder = vbf.newValueBuilder( StringDTO.class );
      builder.prototype().string().set( newName );

      try
      {
         groupsResource.group( groups.get( selectedIndex ).entity().get().identity() ).changeDescription( builder.newInstance() );
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_rename_group_name_already_exist, e );
         }
         throw new OperationException( AdministrationResources.could_not_rename_group, e );
      }
      refresh();
   }

   public void notifyEvent( DomainEvent event )
   {
      for (GroupModel groupModel : groupModels)
      {
         groupModel.notifyEvent( event );
      }
   }
}
