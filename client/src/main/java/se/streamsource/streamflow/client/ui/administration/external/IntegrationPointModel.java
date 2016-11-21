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
package se.streamsource.streamflow.client.ui.administration.external;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.util.Refreshable;

import java.util.Observable;


public class IntegrationPointModel extends Observable
      implements Refreshable
{

   @Structure
   Module module;

   @Uses
   private CommandQueryClient client;

   private ResourceValue resourceValue;

   public IntegrationPointModel( @Uses CommandQueryClient client, @Structure Module module )
   {
      this.client = client;
   }

   public void refresh() throws OperationException
   {
      resourceValue = client.query();
   }

}
