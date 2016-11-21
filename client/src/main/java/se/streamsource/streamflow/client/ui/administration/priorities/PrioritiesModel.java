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
package se.streamsource.streamflow.client.ui.administration.priorities;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.util.DefinitionListModel;
import org.restlet.data.Form;

/**
 * Model for fetching case priorities.
 */
public class PrioritiesModel
      extends DefinitionListModel
{
   public PrioritiesModel()
   {
      super( "create" );

      relationModelMapping("resource", PriorityModel.class);
   }

   public void up( LinkValue selected )
   {
      Form form = new Form();
      form.set( "id", selected.id().get() );
      
      client.postCommand( "up", form );
   }

   public void down( LinkValue selected )
   {
      Form form = new Form();
      form.set( "id", selected.id().get() );

      client.postCommand( "down", form );
   }
}
