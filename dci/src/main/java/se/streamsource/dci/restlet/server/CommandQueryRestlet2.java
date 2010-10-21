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

import java.util.List;

import org.qi4j.api.cache.CacheOptions;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Uniform;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.RoleMap;

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
         request.getAttributes().put( "segments", segments );

         Usecase usecase = UsecaseBuilder.buildUsecase( getUsecaseName( request ) ).with( request.getMethod().isSafe() ? CacheOptions.ALWAYS : CacheOptions.NEVER ).newUsecase();
         UnitOfWork uow = uowf.newUnitOfWork( usecase );

         RoleMap.newCurrentRoleMap();
         try
         {
            // Start handling the build-up for the context
            Uniform resource = createRoot(request, response);
            resource.handle(request, response);

            if (request.getMethod().isSafe())
               uow.discard();
            else
               uow.complete();
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
}
