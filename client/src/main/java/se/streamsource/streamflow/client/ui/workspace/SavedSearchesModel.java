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
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.administration.organization.LinksListModel;

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

   @Override
   public void refresh()
   {
      eventList.clear();
      ValueBuilder<LinkValue> builder = vbf.newValueBuilder( LinkValue.class);
      builder.prototype().text().set( "This week" );
      builder.prototype().id().set("skapad:vecka");
      builder.prototype().href().set( "123" );

      eventList.add( builder.newInstance() );
   }

   public void saveSearch(String name, String query)
   {
      try
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
         builder.prototype().string().set( query );
         client.postCommand( "save", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_perform_operation, e);
      }
   }

   public void remove( LinkValue removeSearch)
   {
      try
      {
         client.getClient( removeSearch ).delete();
      } catch (ResourceException e)
      {
         e.printStackTrace();
      }
   }

}
