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

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.dci.value.StringValue;

/**
 * Model containing priority info
 */
public class PriorityModel
   extends ResourceModel<PriorityValue>
{
   @Uses
   private CommandQueryClient client;

   public void changeColor( String color )
   {
      if( !color.equals( getIndex().color().get() ) )
      {
         Form form = new Form();
         form.add( "color", color );

         client.postCommand( "changecolor", form );
      }
   }

   public void changeDescription( String description )
   {
      if( !getIndex().text().equals( description ) )
      {
         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
         builder.prototype().string().set( description );

         client.postCommand( "changedescription", builder.newInstance() );
      }
   }
}
