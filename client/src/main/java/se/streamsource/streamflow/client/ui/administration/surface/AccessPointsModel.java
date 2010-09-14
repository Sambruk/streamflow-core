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

package se.streamsource.streamflow.client.ui.administration.surface;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.LinkValueListModel;
import se.streamsource.streamflow.client.ui.caze.CaseLabelsModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventParameters;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;
import se.streamsource.streamflow.resource.caze.CaseValue;

import java.util.Observable;


public class AccessPointsModel
   extends LinkValueListModel
      implements EventListener, Refreshable
{
   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   @Uses
   CommandQueryClient client;

   WeakModelMap<String, AccessPointModel> accessPointModels = new WeakModelMap<String, AccessPointModel>()
   {
      protected AccessPointModel newModel( String key )
      {
         CaseLabelsModel labelsModel = obf.newObjectBuilder( CaseLabelsModel.class )
               .use( client.getSubClient( key ).getSubClient( "labels" ) ).newInstance();
         return obf.newObjectBuilder( AccessPointModel.class ).use(
               client.getSubClient( key ),
               labelsModel ).newInstance();
      }
   };

   public void refresh() throws OperationException
   {
      try
      {
         // Get AccessPoints list
         LinksValue accessPointsList = client.query( "index", LinksValue.class );
         EventListSynch.synchronize( accessPointsList.links().get(), linkValues );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public EventList<LinkValue> getAccessPointsList()
   {
      return linkValues;
   }

   public void newAccessPoint( String accessPointName )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( accessPointName );
         client.postCommand( "createaccesspoint", builder.newInstance() );
         refresh();
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_create_accesspoint_name_already_exists, e );
         }
         throw new OperationException( AdministrationResources.could_not_create_accesspoint, e );
      }
   }

   public void removeAccessPoint( String id )
   {
      try
      {
         client.getSubClient( id ).delete();
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_accesspoint, e );
      }
   }

   public void changeDescription( LinkValue link, String newName )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( newName );

      try
      {
         client.getSubClient( link.id().get() ).putCommand( "changedescription", builder.newInstance() );
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_rename_accesspoint_name_already_exists, e );
         }
         throw new OperationException( AdministrationResources.could_not_rename_accesspoint, e );
      }
      refresh();

   }

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
      for( AccessPointModel accessPointModel : accessPointModels )
      {
         accessPointModel.notifyEvent( event );
      }
   }

   public AccessPointModel getAccessPointModel( String id )
   {
      return accessPointModels.get( id );
   }
}
