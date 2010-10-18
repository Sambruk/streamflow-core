/*
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

package se.streamsource.streamflow.web.application.security;

import net.sf.ehcache.Element;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Filter;
import org.restlet.security.User;
import se.streamsource.streamflow.server.plugin.authentication.Authenticator;
import se.streamsource.streamflow.web.domain.interaction.security.Authentication;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.plugin.PluginConfiguration;

/**
 * Accept login if user with the given username has the given password
 * in the Streamflow user database.
 */
public class AuthenticationFilter extends Filter
{
   private static Usecase usecase = UsecaseBuilder.newUsecase( "Verify password" );

   Configuration<PluginConfiguration> config;

   Caching caching;

   @Structure
   UnitOfWorkFactory uowf;

   public AuthenticationFilter( @Uses Context context, @Uses Restlet next, @Uses Caching caching, @Uses Configuration<PluginConfiguration> config )
   {
      super( context, next );
      this.caching = caching;
      this.config = config;
   }

   @Override
   protected int beforeHandle( Request request, Response response )
   {
      ChallengeResponse challengeResponse = request.getChallengeResponse();

      if (challengeResponse == null)
      {
         response.setStatus( Status.CLIENT_ERROR_UNAUTHORIZED );
         response.getChallengeRequests().add( new ChallengeRequest( ChallengeScheme.HTTP_BASIC, "Streamflow" ) );
         return Filter.STOP;
      } else
      {
         String username = challengeResponse.getIdentifier();
         String password = new String( challengeResponse.getSecret() );
         Element element = caching.get( username );
         if (element != null)
         {
            setUserCredentials( request, username );
            return Filter.CONTINUE;
         }

         if (config.configuration().enabled().get())
         {
            ClientResource clientResource = new ClientResource( config.configuration().url().get() );

            clientResource.setChallengeResponse( ChallengeScheme.HTTP_BASIC, username, password );

            // Call plugin
            clientResource.get();

            // Parse response
            if (!Status.SUCCESS_OK.equals( clientResource.getResponse().getStatus() ))
            {
               // Todo Check if errormessage indicates that remote
               // service was unavailable, then look for user in local entitystore
               response.setStatus( clientResource.getResponse().getStatus() );
               response.setEntity( clientResource.getResponseEntity() );
               return Filter.STOP;

            }

            caching.put( new Element( username, username ) );

            // Todo Add or Update user in local entitystore

            setUserCredentials( request, username );

         } else
         {
            UnitOfWork unitOfWork = uowf.newUnitOfWork( usecase );

            try
            {
               try
               {
                  Authentication user = unitOfWork.get( Authentication.class, username );
                  if (user.login( password ))
                  {
                     caching.put( new Element( username, username ) );
                     setUserCredentials( request, username );
                  }

               } catch (NoSuchEntityException e)
               {
                  response.setStatus( Status.CLIENT_ERROR_UNAUTHORIZED );
                  response.setEntity( Authenticator.error.authentication_bad_username_password.toString(), MediaType.TEXT_PLAIN );
                  return Filter.STOP;
               }

            } finally
            {
               unitOfWork.discard();
            }
         }
         return Filter.CONTINUE;
      }
   }

   private void setUserCredentials( Request request, String username )
   {
      request.getClientInfo().setUser( new User( username ) );
      getContext().getDefaultEnroler().enrole( request.getClientInfo() );
   }
}