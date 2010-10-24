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

import java.io.IOException;

import net.sf.ehcache.Element;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Filter;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.streamflow.domain.contact.Contactable;
import se.streamsource.streamflow.server.plugin.authentication.Authenticator;
import se.streamsource.streamflow.server.plugin.authentication.UserDetailsValue;
import se.streamsource.streamflow.web.domain.interaction.security.Authentication;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.plugin.PluginConfiguration;

/**
 * Accept login if user with the given username has the given password in the
 * Streamflow user database.
 */
public class AuthenticationFilter extends Filter
{
   private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
   
   private static Usecase usecase = UsecaseBuilder.newUsecase("Verify password");

   Configuration<PluginConfiguration> config;

   Caching caching;

   @Structure
   UnitOfWorkFactory uowf;
   
   @Structure
   ValueBuilderFactory vbf;

   public AuthenticationFilter(@Uses Context context, @Uses Restlet next, @Uses Caching caching,
         @Uses Configuration<PluginConfiguration> config)
   {
      super(context, next);
      this.caching = caching;
      this.config = config;
   }

   @Override
   protected int beforeHandle(Request request, Response response)
   {
      ChallengeResponse challengeResponse = request.getChallengeResponse();

      if (challengeResponse == null)
      {
         response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
         response.getChallengeRequests().add(new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "Streamflow"));
         return Filter.STOP;
      } else
      {

         UnitOfWork unitOfWork = uowf.newUnitOfWork(usecase);
         try
         {
            String username = challengeResponse.getIdentifier();
            String password = new String(challengeResponse.getSecret());

            Authentication user = unitOfWork.get(Authentication.class, username);

            Element element = caching.get(username);

            if (element != null && user != null && user.login(password))
            {
               setUserCredentials(request, username);
               return Filter.CONTINUE;
            }

            if (config.configuration().enabled().get())
            {
               ClientResource clientResource = new ClientResource(config.configuration().url().get());

               clientResource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, username, password);

               // Call plugin
               Representation result = clientResource.get();

               // Parse response
               if (!Status.SUCCESS_OK.equals(clientResource.getResponse().getStatus()))
               {
                  // Todo Check if errormessage indicates that remote
                  // service was unavailable, then look for user in local
                  // entitystore
                  response.setStatus(clientResource.getResponse().getStatus());
                  response.setEntity(clientResource.getResponseEntity());
                  return Filter.STOP;

               }

               String json;
               try
               {
                  json = result.getText();
               } catch (IOException e)
               {
                  logger.info("Could not get userdetails for externally validated user" );
                  return Filter.STOP;
               }
               UserDetailsValue externalUser = vbf.newValueFromJSON( UserDetailsValue.class, json );

               synchronizeUser(externalUser, (Contactable.Data)user, password);
               caching.put(new Element(username, username));

               setUserCredentials(request, username);
               
               logger.debug("User: " + username + " - successfully authenticated agains external system");

            } else
            {
               try
               {
                  if (user.login(password))
                  {
                     setUserCredentials(request, username);
                  } else
                  {
                     response.setStatus( Status.CLIENT_ERROR_UNAUTHORIZED );
                     response.setEntity( Authenticator.error.authentication_bad_username_password.toString(), MediaType.TEXT_PLAIN );
                     return Filter.STOP;
                  }
               } catch (NoSuchEntityException e)
               {
                  response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                  response.setEntity(Authenticator.error.authentication_bad_username_password.toString(),
                        MediaType.TEXT_PLAIN);
                  return Filter.STOP;
               }

            }
         } finally
         {
            unitOfWork.discard();
         }
         return Filter.CONTINUE;
      }
   }

   private void synchronizeUser(UserDetailsValue externalUser, Contactable.Data user, String password)
   {
      
      if (!externalUser.name().get().equals(user.contact().get().name().get()))
      {
         user.contact().get().name().set(externalUser.name().get());
      }
 
      if (!externalUser.emailAddress().get().equals(user.contact().get().emailAddresses().get().get(0).emailAddress().get()))
      {
         user.contact().get().emailAddresses().get().get(0).emailAddress().set(externalUser.emailAddress().get());
      }
      
      if (!externalUser.phoneNumber().get().equals(user.contact().get().phoneNumbers().get().get(0).phoneNumber().get()))
      {
         user.contact().get().phoneNumbers().get().get(0).phoneNumber().set(externalUser.phoneNumber().get());
      }
      
      if (!((Authentication)user).login(password))
      {
         ((UserAuthentication)user).resetPassword(password);
      }
   }

   private void setUserCredentials(Request request, String username)
   {
      request.getClientInfo().setUser(new User(username));
      getContext().getDefaultEnroler().enrole(request.getClientInfo());
   }
}