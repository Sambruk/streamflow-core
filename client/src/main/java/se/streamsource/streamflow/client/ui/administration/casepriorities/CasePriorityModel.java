/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.administration.casepriorities;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.client.ResourceModel;

/**
 * Model containing priority info
 */
public class CasePriorityModel 
   extends ResourceModel<PriorityValue>
{
   @Uses
   private CommandQueryClient client;

   public void changeColor( String color )
   {
      client.postCommand( "changecolor", color );
   }

   public void changeDescription( String description )
   {
      client.postCommand( "changedescription", description );
   }
}
