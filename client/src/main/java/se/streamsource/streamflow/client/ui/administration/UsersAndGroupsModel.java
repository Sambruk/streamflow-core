/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import java.util.Collection;

/**
 */
public class UsersAndGroupsModel
{
   @Uses
   CommandQueryClient client;

   public EventList<TitledLinkValue> getPossibleUsers()
   {
      try
      {
         BasicEventList<TitledLinkValue> list = new BasicEventList<TitledLinkValue>();

         LinksValue listValue = client.query( "possibleusers", LinksValue.class );
         list.addAll( (Collection) listValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh, e );
      }
   }

   public EventList<TitledLinkValue> getPossibleGroups()
   {
      try
      {
         BasicEventList<TitledLinkValue> list = new BasicEventList<TitledLinkValue>();

         LinksValue linksValue = client.query( "possiblegroups", LinksValue.class );
         list.addAll( (Collection) linksValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh, e );
      }
   }

}
