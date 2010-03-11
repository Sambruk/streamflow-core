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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.LinkComparator;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

/**
 * JAVADOC
 */
public class GroupsModel
      implements Refreshable, EventListener
{
   @Uses
   CommandQueryClient client;

   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   BasicEventList<LinkValue> sourceGroups = new BasicEventList<LinkValue>();
   SortedList<LinkValue> groups = new SortedList<LinkValue>( sourceGroups, new LinkComparator() );

   WeakModelMap<String, ParticipantsModel> groupModels = new WeakModelMap<String, ParticipantsModel>()
   {
      @Override
      protected ParticipantsModel newModel( String key )
      {
         return obf.newObjectBuilder( ParticipantsModel.class ).use( client.getSubClient( key ).getSubClient("participants" )).newInstance();
      }
   };

   public EventList<LinkValue> getGroups()
   {
      return groups;
   }

   public void createGroup( String description )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( description );
         client.postCommand( "creategroup",  builder.newInstance() );
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
         client.getSubClient( id ).delete();
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_group, e );
      }
   }

   public void refresh()
   {
      try
      {
         LinksValue groupsList = client.query( "groups", LinksValue.class );
         EventListSynch.synchronize( groupsList.links().get(), sourceGroups );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }


   public ParticipantsModel getGroupModel( String id )
   {
      return groupModels.get( id );
   }

   public void changeDescription( int selectedIndex, String newName )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( newName );

      try
      {
         client.getSubClient( groups.get( selectedIndex ).id().get() ).putCommand( "changedescription",  builder.newInstance() );
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
      for (ParticipantsModel participantsModel : groupModels)
      {
         participantsModel.notifyEvent( event );
      }
   }
}
