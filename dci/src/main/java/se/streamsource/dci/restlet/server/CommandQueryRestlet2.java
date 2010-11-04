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

package se.streamsource.dci.restlet.server;

import org.qi4j.api.cache.CacheOptions;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityState;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Uniform;
import org.restlet.data.*;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.RoleMap;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
   ResultWriter responseWriter;

   @Override
   public void handle( Request request, Response response )
   {
      int tries = 0;

      // TODO Make this number configurable
      while (tries < 10)
      {
         // Root of the call
         Reference ref = request.getResourceRef();
         List<String> segments = ref.getScheme().equals( "riap" ) ? ref.getRelativeRef( new Reference( "riap://application/" ) ).getSegments() : ref.getRelativeRef().getSegments();

         // Handle conversion of verbs into standard interactions
         if (segments.get( segments.size()-1 ).equals(""))
         {
            if (request.getMethod().equals( Method.DELETE ))
            {
               // Translate DELETE into command "delete"
               segments.set( segments.size()-1, "delete" );
            } else if (request.getMethod().equals( Method.PUT ))
            {
               // Translate PUT into command "update"
               segments.set( segments.size()-1, "update" );
            }
         }

         request.getAttributes().put( "segments", segments );
         request.getAttributes().put( "template", new StringBuilder("/rest/") );

         Usecase usecase = UsecaseBuilder.buildUsecase( getUsecaseName( request ) ).with( request.getMethod().isSafe() ? CacheOptions.ALWAYS : CacheOptions.NEVER ).newUsecase();
         UnitOfWork uow = uowf.newUnitOfWork( usecase );

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
                     EntityComposite entity = RoleMap.role( EntityComposite.class );
                     EntityState state = spi.getEntityState( entity );
                     Date lastModified = new Date( state.lastModified());
                     Tag tag = new Tag(state.identity().identity()+"/"+state.version());
                     response.getEntity().setModificationDate( lastModified );
                     response.getEntity().setTag( tag );
                  } catch (IllegalArgumentException e)
                  {
                     // Ignore
                  }
               }

               // Check if characterset is set
               if (response.getEntity().getCharacterSet() == null)
               {
                  response.getEntity().setCharacterSet( CharacterSet.UTF_8);
               }

               // Check if language is set
               if (response.getEntity().getLanguages().isEmpty())
               {
                  response.getEntity().getLanguages().add( Language.ENGLISH );
               }

               uow.discard();
            }
            else
            {
               // Check if last modified and tag is set
               Date lastModified = null;
               Tag tag = null;
               try
               {
                  EntityComposite entity = RoleMap.role( EntityComposite.class );
                  EntityState state = spi.getEntityState( entity );
                  lastModified = new Date( state.lastModified());
                  tag = new Tag(state.identity().identity()+"/"+state.version());
               } catch (IllegalArgumentException e)
               {
                  // Ignore
               }

               uow.complete();

               Object result = commandResult.getResult();
               if (result != null)
               {
                  if (result instanceof Representation)
                     response.setEntity( (Representation) result);
                  else
                  {
                     if (!responseWriter.write( result, response ))
                        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not write result of type "+result.getClass().getName());
                  }

                  if (response.getEntity() != null)
                  {
                     // Check if characterset is set
                     if (response.getEntity().getCharacterSet() == null)
                     {
                        response.getEntity().setCharacterSet( CharacterSet.UTF_8);
                     }

                     // Check if language is set
                     if (response.getEntity().getLanguages().isEmpty())
                     {
                        response.getEntity().getLanguages().add( Language.ENGLISH );
                     }

                     // Check if last modified and tag is set
                     if (lastModified != null)
                     {
                        response.getEntity().setModificationDate( lastModified );
                        response.getEntity().setTag( tag );
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
            handleException( response, e );
            return;
         } finally
         {
            RoleMap.clearCurrentRoleMap();
         }
      }
   }

   protected abstract Uniform createRoot( Request request, Response response );

   private String getUsecaseName( Request request )
   {
      if (request.getMethod().equals( org.restlet.data.Method.DELETE ))
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
         response.setEntity( new StringRepresentation( e.getMessage() ) );
         response.setStatus( e.getStatus() );
      } catch (IllegalArgumentException e)
      {
         // IAE (or subclasses) are considered client faults
         response.setEntity( new StringRepresentation( e.getMessage() ) );
         response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
      } catch (RuntimeException e)
      {
         // RuntimeExceptions are considered server faults
         LoggerFactory.getLogger( getClass() ).warn( "Exception thrown during processing", e );
         response.setEntity( new StringRepresentation( e.getMessage() ) );
         response.setStatus( Status.SERVER_ERROR_INTERNAL );
      } catch (Exception e)
      {
         // Checked exceptions are considered client faults
         response.setEntity( new StringRepresentation( e.getMessage() ) );
         response.setStatus( Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY );
      } catch (Throwable e)
      {
         // Anything else are considered server faults
         LoggerFactory.getLogger( getClass() ).error( "Exception thrown during processing", e );
         response.setEntity( new StringRepresentation( e.getMessage() ) );
         response.setStatus( Status.SERVER_ERROR_INTERNAL );
      }
   }

   private Variant getVariant( Request request )
   {
      List<Language> possibleLanguages = Arrays.asList( Language.ENGLISH );
      Language language = request.getClientInfo().getPreferredLanguage( possibleLanguages );

      if (language == null)
         language = Language.ENGLISH;

      List<MediaType> possibleMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM );
      MediaType responseType = request.getClientInfo().getPreferredMediaType( possibleMediaTypes );

      if (responseType == null)
         responseType = MediaType.TEXT_HTML;

      Variant variant = new Variant( responseType, language );
      variant.setCharacterSet( CharacterSet.UTF_8 );

      return variant;
   }
}
