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
package se.streamsource.streamflow.web.application.defaults;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.routing.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rest filter reacting on AvailabilityService circuit breaker on/off.
 */
public class AvailabilityFilter extends Filter
{

   private static final Logger logger = LoggerFactory.getLogger( AvailabilityFilter.class );

   private AvailabilityService availabilityService;

   public AvailabilityFilter( @Uses Context context, @Uses Restlet next, @Uses AvailabilityService availabilityService )
   {
      super( context, next );
      this.availabilityService = availabilityService;
   }

   @Override
   protected int beforeHandle( Request request, Response response )
   {
      if( availabilityService.isAvailable() )
         return Filter.CONTINUE;
      else
      {
         logger.info( "Report Streamflow unavailable due to maintenance." );
         response.setStatus( Status.SERVER_ERROR_SERVICE_UNAVAILABLE );
         return Filter.STOP;
      }
   }
}
