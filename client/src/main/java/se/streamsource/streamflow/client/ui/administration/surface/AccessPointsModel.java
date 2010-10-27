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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.LinkValueListModel;


public class AccessPointsModel
   extends LinkValueListModel
      implements Refreshable
{
   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   @Uses
   CommandQueryClient client;

/*
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

*/

   public void createAccessPoint( String accessPointName )
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( accessPointName );
         client.postCommand( "createaccesspoint", builder.newInstance() );
      } catch (ResourceException e)
      {
         if (Status.CLIENT_ERROR_CONFLICT.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_create_accesspoint_name_already_exists, e );
         }
         throw new OperationException( AdministrationResources.could_not_create_accesspoint, e );
      }
   }

   public void removeAccessPoint( LinkValue id )
   {
      client.getClient( id ).delete();
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
      }
   }
}
