/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.status;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.client.LoggerCategories;

import java.util.logging.Logger;

/**
 * Helper service for setting status in the statusbar. The possible
 * messages comes from an enum to make it easy to know what texts
 * are available.
 */
@Mixins(StatusService.Mixin.class)
public interface StatusService
      extends ServiceComposite
{
   void status( StatusResources status );

   abstract class Mixin
         implements StatusService
   {
      public void status( StatusResources status )
      {
         Logger.getLogger( LoggerCategories.STATUS ).info( status.name() );
      }
   }
}
