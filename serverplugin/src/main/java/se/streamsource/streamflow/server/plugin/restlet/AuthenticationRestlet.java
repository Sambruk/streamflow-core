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

package se.streamsource.streamflow.server.plugin.restlet;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * Empty restlet...
 */
public class AuthenticationRestlet
      extends Restlet
{

   @Override
   public void handle( Request request, Response response )
   {
      super.handle( request, response );

      try
      {
         if (request.getMethod().equals( Method.GET ))
         {
            response.setStatus( Status.SUCCESS_NO_CONTENT );
         } else
         {
            response.setStatus( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
         }
      } finally
      {
         request.release();
      }
   }
}
