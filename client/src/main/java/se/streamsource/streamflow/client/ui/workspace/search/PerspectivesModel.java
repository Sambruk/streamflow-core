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

package se.streamsource.streamflow.client.ui.workspace.search;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.LinkValueListModel;
import se.streamsource.streamflow.resource.user.profile.PerspectiveValue;

import java.util.List;

/**
 * JAVADOC
 */
public class PerspectivesModel
      extends LinkValueListModel
{
   @Structure
   ValueBuilderFactory vbf;

   public void remove( LinkValue link )
   {
      if (link != null)
      {
         client.getClient( link ).delete();
      }
   }

   public void refresh()
   {
      List<LinkValue> links = client.query( "index", LinksValue.class ).links().get();
      EventListSynch.synchronize( links, linkValues );
   }

   public void changeDescription( LinkValue link, String name )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( name );
      client.getClient( link ).postCommand( "changedescription", builder.newInstance() );
   }

   public void savePerspective( PerspectiveValue perspective )
   {
      client.postCommand( "create", perspective );
   }
}
