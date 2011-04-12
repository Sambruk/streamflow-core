/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.application.security;

import org.qi4j.api.injection.scope.*;
import org.restlet.*;
import org.restlet.routing.*;
import org.slf4j.*;

/**
 * Accept login if user with the given username has the given password in the
 * Streamflow user database.
 */
public class AuthenticationFilter extends Filter
{
   @Service
   AuthenticationFilterService filterService;

  
   public AuthenticationFilter(@Uses Context context, @Uses Restlet next, @Uses AuthenticationFilterService filterService)
   {
      super(context, next);
      this.filterService = filterService;
   }

   @Override
   protected int beforeHandle(Request request, Response response)
   {
      int result = filterService.beforeHandle(request, response, getContext());

      if (result == Filter.CONTINUE)
         MDC.put( "user", request.getClientInfo().getUser().getName());

      return result;
   }

   @Override
   protected void afterHandle( Request request, Response response )
   {
      MDC.remove( "user" );

      super.afterHandle( request, response );
   }
}