/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.spi.service.ServiceDescriptor;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;
import se.streamsource.infrastructure.circuitbreaker.service.ServiceCircuitBreaker;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.server.plugin.authentication.Authenticator;
import se.streamsource.streamflow.server.plugin.authentication.UserDetailsValue;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.interaction.security.Authentication;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

import java.io.IOException;

@Mixins(AuthenticationFilterService.Mixin.class)
public interface AuthenticationFilterService extends ServiceComposite, Configuration, Activatable, ServiceCircuitBreaker
{
   public int beforeHandle(Request request, Response response, Context context);

   abstract class Mixin
         implements AuthenticationFilterService
   {

      private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

      private static Usecase verifyUsecase = UsecaseBuilder.newUsecase("Verify password");
      private static Usecase addUsecase = UsecaseBuilder.newUsecase("Add new user");
      private static Usecase updateUsecase = UsecaseBuilder.newUsecase("Update user");

      @Uses
      ServiceDescriptor descriptor;

      @This
      Configuration<AuthenticationFilterServiceConfiguration> config;

      @Service
      CachingService cachingService;

      Caching caching;

      @Structure
      Module module;

      CircuitBreaker circuitBreaker;


      public void activate() throws Exception
      {
         circuitBreaker = descriptor.metaInfo( CircuitBreaker.class );

         config.configuration();

         caching = new Caching( cachingService, Caches.VERIFIEDUSERS );
      }

      public void passivate() throws Exception
      {
         logger.info( "Passivated" );
      }

      public CircuitBreaker getCircuitBreaker()
      {
         return circuitBreaker;
      }

      public int beforeHandle(Request request, Response response, Context context)
      {
         ChallengeResponse challengeResponse = request.getChallengeResponse();

         if (challengeResponse == null)
         {
            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            response.getChallengeRequests().add(new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "Streamflow"));
            return Filter.STOP;
         } else
         {

            String username = challengeResponse.getIdentifier();
            String password = new String(challengeResponse.getSecret());

            UnitOfWork unitOfWork = module.unitOfWorkFactory().newUnitOfWork(verifyUsecase);

            Authentication localUser = null;
            try
            {
               try
               {
                  localUser = unitOfWork.get(Authentication.class, username);
               } catch (NoSuchEntityException e)
               {
                  // This is ok
               }

               Element cachedUser = caching.get(username);

               if (cachedUser != null && localUser != null && localUser.login(password))
               {
                  setUserCredentials(request, context, username);
                  return Filter.CONTINUE;
               }

               boolean authorized = false;

               if (!UserEntity.ADMINISTRATOR_USERNAME.equals(username) && config.configuration().enabled().get() && circuitBreaker.isOn())
               {
                  ClientResource clientResource = new ClientResource(config.configuration().url().get() + "/authentication/userdetails" );

                  clientResource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, username, password);

                  try
                  {
                     // Call plugin
                     Representation result = clientResource.get();

                     String json;
                     try
                     {
                        json = result.getText();
                     } catch (IOException e)
                     {
                        throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED,
                              "Could not get userdetails for externally validated user");
                     }
                     UserDetailsValue externalUser = module.valueBuilderFactory().newValueFromJSON(UserDetailsValue.class, json);

                     if (localUser == null)
                     {
                        createNewUser(externalUser, username, password);
                     } else
                     {
                        updateUser(externalUser, (Contactable.Data) localUser, password);
                     }

                     caching.put(new Element(username, username));

                     setUserCredentials(request, context, username);
                     authorized = true;
                     logger.debug("User: " + username + " - successfully authenticated agains external system");
                     circuitBreaker.success();

                  } catch (ResourceException e)
                  {
                     if (Status.CLIENT_ERROR_UNAUTHORIZED.equals(clientResource.getResponse().getStatus()))
                     {
                        circuitBreaker.success();

                        // Authentication failed
                        response.setStatus(clientResource.getResponse().getStatus());
                        response.getChallengeRequests()
                              .add(new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "Streamflow"));
                        return Filter.STOP;

                     } else
                     {
                        // Send this to CircuitBreaker
                        circuitBreaker.throwable( e );
                     }

                     if (localUser != null && localUser.login(password))
                     {
                        authorized = true;
                        setUserCredentials(request, context, username);
                     }
                  }
               } else
               {
                  if (localUser != null && localUser.login(password))
                  {
                     authorized = true;
                     setUserCredentials(request, context, username);
                  }
               }

               if (!authorized)
               {
                  response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                  response.getChallengeRequests().add(new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "Streamflow"));
                  response.setEntity(Authenticator.error.authentication_bad_username_password.toString(),
                        MediaType.TEXT_PLAIN);
                  return Filter.STOP;
               }

            } finally
            {
               unitOfWork.discard();
            }

            return Filter.CONTINUE;
         }
      }

      private void updateUser(UserDetailsValue externalUser, Contactable.Data user, String password)
      {
         boolean modified = false;
         if (!externalUser.name().get().equals(user.contact().get().name().get()))
         {
            modified = true;
         }

         if (!externalUser.emailAddress().get()
               .equals(user.contact().get().emailAddresses().get().get(0).emailAddress().get()))
         {
            modified = true;
         }

         if (!externalUser.phoneNumber().get()
               .equals(user.contact().get().phoneNumbers().get().get(0).phoneNumber().get()))
         {
            modified = true;
         }

         if (!((Authentication) user).login(password))
         {
            modified = true;
         }

         if (modified)
         {
            try
            {
               UnitOfWork unitOfWork = module.unitOfWorkFactory().newUnitOfWork(updateUsecase);
               Organization org = unitOfWork.get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID ).organization().get();
               UserEntity userEntity = unitOfWork.get(UserEntity.class, EntityReference.getEntityReference( user )
                     .identity());
               userEntity.resetPassword(password);
               userEntity.join( org );

               ValueBuilder<ContactDTO> contactBuilder = module.valueBuilderFactory().newValueBuilder(ContactDTO.class);
               contactBuilder.prototype().name().set(externalUser.name().get());
               ValueBuilder<ContactEmailDTO> emailBuilder = module.valueBuilderFactory().newValueBuilder(ContactEmailDTO.class);
               emailBuilder.prototype().emailAddress().set(externalUser.emailAddress().get());
               contactBuilder.prototype().emailAddresses().get().add(emailBuilder.newInstance());
               ValueBuilder<ContactPhoneDTO> phoneBuilder = module.valueBuilderFactory().newValueBuilder(ContactPhoneDTO.class);
               phoneBuilder.prototype().phoneNumber().set(externalUser.phoneNumber().get());
               contactBuilder.prototype().phoneNumbers().get().add(phoneBuilder.newInstance());
               ((Contactable) userEntity).updateContact(contactBuilder.newInstance());

               unitOfWork.complete();
            } catch (ConcurrentEntityModificationException e)
            {
               throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, "Could not update user in local repository");
            } catch (UnitOfWorkCompletionException e)
            {
               throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, "Could not update user in local repository");
            }
         }
      }

      private void createNewUser(UserDetailsValue externalUser, String username, String password)
      {
         try
         {
            UnitOfWork unitOfWork = module.unitOfWorkFactory().newUnitOfWork(addUsecase);
            UsersEntity usersEntity = unitOfWork.get(UsersEntity.class, UsersEntity.USERS_ID);
            Organization org = unitOfWork.get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID ).organization().get();
            se.streamsource.streamflow.web.domain.structure.user.User user = usersEntity.createUser(username, password);
            user.join( org );

            ValueBuilder<ContactDTO> contactBuilder = module.valueBuilderFactory().newValueBuilder(ContactDTO.class);
            contactBuilder.prototype().name().set(externalUser.name().get());
            ValueBuilder<ContactEmailDTO> emailBuilder = module.valueBuilderFactory().newValueBuilder(ContactEmailDTO.class);
            emailBuilder.prototype().emailAddress().set(externalUser.emailAddress().get());
            contactBuilder.prototype().emailAddresses().get().add(emailBuilder.newInstance());
            ValueBuilder<ContactPhoneDTO> phoneBuilder = module.valueBuilderFactory().newValueBuilder(ContactPhoneDTO.class);
            phoneBuilder.prototype().phoneNumber().set(externalUser.phoneNumber().get());
            contactBuilder.prototype().phoneNumbers().get().add(phoneBuilder.newInstance());
            ((Contactable) user).updateContact(contactBuilder.newInstance());

            unitOfWork.complete();
         } catch (ConcurrentEntityModificationException e)
         {
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, "Could not add user to local repository");
         } catch (UnitOfWorkCompletionException e)
         {
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, "Could not add user to local repository");
         }
      }

      private void setUserCredentials(Request request, Context context, String username)
      {
         request.getClientInfo().setUser(new User(username));
         context.getDefaultEnroler().enrole(request.getClientInfo());
      }
   }
}
