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

package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.TitledLinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.organization.LinksListModel;
import se.streamsource.streamflow.resource.user.profile.SearchValue;

import java.util.List;

/**
 * JAVADOC
 */
public class SavedSearchesModel
      extends LinksListModel
{
   @Structure
   ValueBuilderFactory vbf;

   public SavedSearchesModel( @Uses CommandQueryClient client )
   {
      super( client, "index" );
   }

   public void saveSearch( SearchValue saveSearch )
   {
      try
      {
         client.postCommand( "addsearch", saveSearch );
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_perform_operation, e );
      }
   }

   public void remove( LinkValue link )
   {
      try
      {
         if (link != null)
         {
            client.getClient( link ).delete();
            eventList.remove( link );
         }

      } catch (ResourceException e)
      {
         e.printStackTrace();
      }
   }

   public void refresh()
   {
      try
      {
         List<LinkValue> links = client.query( "index", TitledLinksValue.class ).links().get();
         EventListSynch.synchronize( links, eventList );
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_refresh, e );
      }
   }

   public void updateSearch( LinkValue link, SearchValue searchValue )
   {
      try
      {
         client.getClient( link ).postCommand( "update", searchValue );
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_perform_operation, e );
      }
   }

   public void changeDescription( LinkValue link, String name )
   {

      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( name );
         client.getClient( link ).postCommand( "changedescription", builder.newInstance() );
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_perform_operation, e );
      }
   }

   public void changeQuery( LinkValue link, String query )
   {

      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( query );
         client.getClient( link ).postCommand( "changequery", builder.newInstance() );
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_perform_operation, e );
      }
   }
}
