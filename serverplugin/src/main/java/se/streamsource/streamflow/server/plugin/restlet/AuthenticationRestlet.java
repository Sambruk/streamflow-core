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
package se.streamsource.streamflow.server.plugin.restlet;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.server.plugin.authentication.Authenticator;
import se.streamsource.streamflow.server.plugin.authentication.UserDetailsValue;
import se.streamsource.streamflow.server.plugin.authentication.UserIdentityValue;

/**
 * Empty restlet...
 */
public class AuthenticationRestlet extends Restlet
{
   @Structure
   Module module;

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
               ValueBuilder<UserIdentityValue> builder = module.valueBuilderFactory().newValueBuilder(UserIdentityValue.class);
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
