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

package se.streamsource.dci.restlet.server;

import org.qi4j.api.cache.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.usecase.*;
import org.qi4j.api.value.*;
import org.qi4j.spi.*;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import org.slf4j.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.restlet.server.api.*;

import java.util.*;

/**
 * JAVADOC
 */
public abstract class CommandQueryRestlet2
        extends Restlet
{
   private
   @Structure
   UnitOfWorkFactory uowf;

   private
   @Structure
   Qi4jSPI spi;

   @Structure
   private ValueBuilderFactory vbf;

   protected
   @Structure
   Module module;

   @Service
   CommandResult commandResult;

   @Service
   ResultWriterDelegator responseWriter;

   private Map<Class, Uniform> subResources = Collections.synchronizedMap(new HashMap<Class, Uniform>());

   @Override
   public void handle(Request request, Response response)
   {
      MDC.put("url", request.getResourceRef().toString());

      try
      {
         int tries = 0;

         // TODO Make this number configurable
         while (tries < 10)
         {
            // Root of the call
            Reference ref = request.getResourceRef();
            List<String> segments = ref.getScheme().equals("riap") ? ref.getRelativeRef(new Reference("riap://application/")).getSegments() : ref.getRelativeRef().getSegments();

            // Handle conversion of verbs into standard interactions
            if (segments.get(segments.size() - 1).equals(""))
            {
               if (request.getMethod().equals(Method.DELETE))
               {
                  // Translate DELETE into command "delete"
                  segments.set(segments.size() - 1, "delete");
               } else if (request.getMethod().equals(Method.PUT))
               {
                  // Translate PUT into command "update"
                  segments.set(segments.size() - 1, "update");
               }
            }

            request.getAttributes().put("segments", segments);
            request.getAttributes().put("template", new StringBuilder("/rest/"));

            Usecase usecase = UsecaseBuilder.buildUsecase(getUsecaseName(request)).with(request.getMethod().isSafe() ? CacheOptions.ALWAYS : CacheOptions.NEVER).newUsecase();
            UnitOfWork uow = uowf.newUnitOfWork(usecase);

            RoleMap.newCurrentRoleMap();
            try
            {
               // Start handling the build-up for the context
               Uniform resource = createRoot(request, response);
               resource.handle(request, response);

               if (response.getEntity() != null)
               {
                  if (response.getEntity().getModificationDate() == null)
                  {

                     try
                     {
                        ResourceValidity validity = RoleMap.role(ResourceValidity.class);
                        validity.updateResponse(response);
                     } catch (IllegalArgumentException e)
                     {
                        // Ignore
                     }
                  }

                  // Check if characterset is set
                  if (response.getEntity().getCharacterSet() == null)
                  {
                     response.getEntity().setCharacterSet(CharacterSet.UTF_8);
                  }

                  // Check if language is set
                  if (response.getEntity().getLanguages().isEmpty())
                  {
                     response.getEntity().getLanguages().add(Language.ENGLISH);
                  }

                  uow.discard();
               } else
               {
                  // Check if last modified and tag is set
                  ResourceValidity validity = null;

                  try
                  {
                     validity = RoleMap.role(ResourceValidity.class);
                  } catch (IllegalArgumentException e)
                  {
                     // Ignore
                  }

                  uow.complete();

                  Object result = commandResult.getResult();
                  if (result != null)
                  {
                     if (result instanceof Representation)
                        response.setEntity((Representation) result);
                     else
                     {
                        if (!responseWriter.write(result, response))
                           throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not write result of type " + result.getClass().getName());
                     }

                     if (response.getEntity() != null)
                     {
                        // Check if characterset is set
                        if (response.getEntity().getCharacterSet() == null)
                        {
                           response.getEntity().setCharacterSet(CharacterSet.UTF_8);
                        }

                        // Check if language is set
                        if (response.getEntity().getLanguages().isEmpty())
                        {
                           response.getEntity().getLanguages().add(Language.ENGLISH);
                        }

                        // Check if last modified and tag should be set
                        if (validity != null)
                        {
                           UnitOfWork lastModifiedUoW = uowf.newUnitOfWork();

                           try
                           {
                              validity.updateEntity(lastModifiedUoW);

                              validity.updateResponse(response);
                           } finally
                           {
                              lastModifiedUoW.discard();
                           }
                        }
                     }

                  }
                  return;
               }
               return;

            } catch (ConcurrentEntityModificationException ex)
            {
               uow.discard();

               // Try again
            } catch (Throwable e)
            {
               uow.discard();
               handleException(response, e);
               return;
            } finally
            {
               RoleMap.clearCurrentRoleMap();
            }
         }
      } finally
      {
         MDC.clear();
      }
   }

   protected abstract Uniform createRoot(Request request, Response response);

   // Callbacks used from resources
   public void subResource(Class<? extends CommandQueryResource> subResourceClass)
   {
      Uniform subResource = subResources.get(subResourceClass);

      if (subResource == null)
      {
         // Instantiate and store subresource instance
         subResource = module.objectBuilderFactory().newObjectBuilder(subResourceClass).use(this).newInstance();
         subResources.put(subResourceClass, subResource);
      }

      subResource.handle(Request.getCurrent(), Response.getCurrent());
   }

   public void subResourceContexts(Class<?>[] contextClasses)
   {
      module.objectBuilderFactory().newObjectBuilder(DefaultCommandQueryResource.class).use(new Object[]{contextClasses}).newInstance().handle(Request.getCurrent(), Response.getCurrent());
   }

   private String getUsecaseName(Request request)
   {
      if (request.getMethod().equals(org.restlet.data.Method.DELETE))
         return "delete";
      else
         return request.getResourceRef().getLastSegment();
   }

   private void handleException(Response response, Throwable ex)
   {
      try
      {
         throw ex;
      } catch (ResourceException e)
      {
         // IAE (or subclasses) are considered client faults
         LoggerFactory.getLogger(getClass()).debug("ResourceException thrown during processing", e);
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(e.getStatus());
      } catch (IllegalArgumentException e)
      {
         // IAE (or subclasses) are considered client faults
         LoggerFactory.getLogger(getClass()).debug("IllegalArgumentsException thrown during processing", e);
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
      } catch (RuntimeException e)
      {
         // RuntimeExceptions are considered server faults
         LoggerFactory.getLogger(getClass()).warn("Exception thrown during processing", e);
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(Status.SERVER_ERROR_INTERNAL);
      } catch (Exception e)
      {
         // Checked exceptions are considered client faults
         LoggerFactory.getLogger(getClass()).debug("Checked exception thrown during processing", e);
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
      } catch (Throwable e)
      {
         // Anything else are considered server faults
         LoggerFactory.getLogger(getClass()).error("Exception thrown during processing", e);
         response.setEntity(new StringRepresentation(e.getMessage()));
         response.setStatus(Status.SERVER_ERROR_INTERNAL);
      }
   }
}
