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

package se.streamsource.streamflow.server.plugin.restlet;

import org.qi4j.api.common.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import se.streamsource.streamflow.server.plugin.authentication.*;

/**
 * Empty restlet...
 */
public class AuthenticationRestlet extends Restlet
{
   @Structure
   ValueBuilderFactory vbf;

   @Optional
   @Service
   Authenticator authenticator;

   @Override
   public void handle(Request request, Response response)
   {
      super.handle(request, response);

      try
      {
         if (request.getMethod().equals(Method.GET))
         {
            if (request.getChallengeResponse() == null)
            {
               response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
               response.getChallengeRequests().add(new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "Streamflow"));
            } else
            {
               ValueBuilder<UserIdentityValue> builder = vbf.newValueBuilder(UserIdentityValue.class);
               builder.prototype().username().set(request.getChallengeResponse().getIdentifier());
               builder.prototype().password().set(new String(request.getChallengeResponse().getSecret()));

               try
               {
                  if ("userdetails".equalsIgnoreCase(request.getResourceRef().getLastSegment()))
                  {
                     UserDetailsValue userDetailsValue = authenticator.userdetails(builder.newInstance());
                     StringRepresentation result = new StringRepresentation(userDetailsValue.toJSON(),
                           MediaType.APPLICATION_JSON, Language.DEFAULT, CharacterSet.UTF_8);
                     response.setStatus(Status.SUCCESS_OK);
                     response.setEntity(result);
                  } else
                  {
                     authenticator.authenticate(builder.newInstance());
                     response.setStatus(Status.SUCCESS_NO_CONTENT);
                  }
               } catch (ResourceException e)
               {
                  response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                  response.setEntity(e.getStatus().getDescription(), MediaType.TEXT_PLAIN);
               }

            }
         } else

         {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
         }
      } finally
      {
         request.release();
      }
   }
}
