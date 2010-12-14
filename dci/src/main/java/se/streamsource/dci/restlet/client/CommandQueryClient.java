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

package se.streamsource.dci.restlet.client;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.*;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinkValue;

import java.io.IOException;

/**
 * Base class for client-side Command/Query resources
 */
public class CommandQueryClient
{
   @Uses
   private CommandQueryClientFactory cqcFactory;

   @Uses
   private Reference reference;

   private ResourceValue resourceValue;

   public Reference getReference()
   {
      return reference;
   }

   public ResourceValue getResource()
   {
      return resourceValue;
   }

   public synchronized ResourceValue queryResource( ) throws ResourceException
   {
      return resourceValue = query( "", null, ResourceValue.class );
   }

   public synchronized <T extends ValueComposite> T query( String operation, Class<T> queryResult ) throws ResourceException
   {
      return query( operation, null, queryResult );
   }

   public synchronized <T extends ValueComposite> T query( String operation, ValueComposite queryValue, Class<T> queryResult ) throws ResourceException
   {
      Response response = invokeQuery( operation, queryValue );

      if (response.getStatus().isSuccess())
      {
         cqcFactory.updateCache( response );

         String jsonValue = response.getEntityAsText();

         return cqcFactory.newValue(queryResult, jsonValue );
      } else
      {
         // This will throw an exception
         handleError( response );
         return null;
      }
   }

   public synchronized Representation queryRepresentation( String query, ValueComposite queryValue )
   {
      Response response = invokeQuery( query, queryValue );

      if (response.getStatus().isSuccess())
      {
         return response.getEntity();
      } else
      {
         // This will throw an exception
         handleError( response );
         return null;
      }
   }

   private void setQueryParameters( final Reference ref, ValueComposite queryValue )
   {
      // Value as parameter
      StateHolder holder = cqcFactory.getSPI().getState( queryValue );
      final ValueDescriptor descriptor = cqcFactory.getSPI().getValueDescriptor( queryValue );

      ref.setQuery( null );

      holder.visitProperties( new StateHolder.StateVisitor<RuntimeException>()
      {
         public void visitProperty( QualifiedName
               name, Object value )
         {
            if (value != null)
            {
               PropertyTypeDescriptor propertyDesc = descriptor.state().getPropertyByQualifiedName( name );
               String queryParam = propertyDesc.propertyType().type().toQueryParameter( value );
               ref.addQueryParameter( name.name(), queryParam );
            }
         }
      } );
   }

   public synchronized void postLink( LinkValue link ) throws ResourceException
   {
      postCommand( link.href().get(), new EmptyRepresentation() );
   }

   public synchronized void postCommand( String operation ) throws ResourceException
   {
      postCommand( operation, new EmptyRepresentation() );
   }

   public synchronized void postCommand( String operation, ValueComposite command ) throws ResourceException
   {
      Representation commandRepresentation;
      commandRepresentation = new StringRepresentation( command.toJSON(), MediaType.APPLICATION_JSON, null, CharacterSet.UTF_8 );

      postCommand( operation, commandRepresentation );
   }

   public synchronized void postCommand( String operation, Representation commandRepresentation )
         throws ResourceException
   {
      postCommand( operation, commandRepresentation, cqcFactory.getHandler() );
   }

   public synchronized void postCommand( String operation, Representation commandRepresentation, ResponseHandler responseHandler )
         throws ResourceException
   {
      Reference ref = new Reference( reference.toUri().toString() + operation );
      Request request = new Request( Method.POST, ref, commandRepresentation );

      cqcFactory.updateCommandRequest( request );

      Response response = new Response( request );
      cqcFactory.getClient().handle( request, response );

      try
      {
         if (response.getStatus().isSuccess())
         {
            cqcFactory.updateCache( response );

            responseHandler.handleResponse( response );
         } else
         {
            handleError( response );
         }
      } finally
      {
         response.release();
      }
   }

   private Object handleError( Response response )
         throws ResourceException
   {
      if (response.getStatus().equals( Status.SERVER_ERROR_INTERNAL ))
      {
         if (response.getEntity().getMediaType().equals( MediaType.APPLICATION_JAVA_OBJECT ))
         {
            try
            {
               Object exception = new ObjectRepresentation( response.getEntity() ).getObject();
               throw new ResourceException( (Throwable) exception );
            } catch (IOException e)
            {
               throw new ResourceException( e );
            } catch (ClassNotFoundException e)
            {
               throw new ResourceException( e );
            }
         }

         throw new ResourceException( Status.SERVER_ERROR_INTERNAL, response.getEntityAsText() );
      } else
      {
         if (response.getEntity() != null)
         {
            String text = response.getEntityAsText();
            throw new ResourceException( response.getStatus().getCode(), response.getStatus().getName(), text, response.getRequest().getResourceRef().toUri().toString() );
         } else
         {
            throw new ResourceException( response.getStatus().getCode(), response.getStatus().getName(), response.getStatus().getDescription(), response.getRequest().getResourceRef().toUri().toString() );
         }
      }
   }

   private Response invokeQuery( String operation, ValueComposite queryValue )
         throws ResourceException
   {
      Reference ref = new Reference( reference.toUri().toString() + operation );
      if (queryValue != null)
         setQueryParameters( ref, queryValue );

      Request request = new Request( Method.GET, ref );
      cqcFactory.updateQueryRequest( request );

      Response response = new Response( request );

      cqcFactory.getClient().handle( request, response );

      return response;
   }

   public synchronized void create() throws ResourceException
   {
      putCommand( null );
   }

   public synchronized void putCommand( String operation ) throws ResourceException
   {
      putCommand( operation, null, cqcFactory.getHandler() );
   }

   public synchronized void putCommand( String operation, ValueComposite command ) throws ResourceException
   {
      putCommand( operation, command, cqcFactory.getHandler() );
   }

   public synchronized void putCommand( String operation, ValueComposite command, ResponseHandler responseHandler) throws ResourceException
   {
      Representation commandRepresentation;
      if (command != null)
         commandRepresentation = new StringRepresentation( command.toJSON(), MediaType.APPLICATION_JSON, null, CharacterSet.UTF_8 );
      else
         commandRepresentation = new EmptyRepresentation();

      Reference ref = new Reference( reference.toUri().toString() );

      if (operation != null)
      {
         ref = ref.addSegment( operation );
      }

      Request request = new Request( Method.PUT, ref );
      cqcFactory.updateCommandRequest( request );

      request.setEntity( commandRepresentation );
      int tries = 3;
      while (true)
      {
         try
         {
            Response response = new Response( request );
            cqcFactory.getClient().handle( request, response );

            try
            {
               if (response.getStatus().isSuccess())
               {
                  cqcFactory.updateCache( response );

                  responseHandler.handleResponse( response );
               } else
               {
                  handleError( response );
               }
            } finally
            {
               response.release();
            }
            break;
         } catch (ResourceException e)
         {
            if (e.getStatus().equals( Status.CONNECTOR_ERROR_COMMUNICATION ) ||
                  e.getStatus().equals( Status.CONNECTOR_ERROR_CONNECTION ))
            {
               if (tries == 0)
                  throw e; // Give up
               else
               {
                  // Try again
                  tries--;
                  continue;
               }
            } else
            {
               // Abort
               throw e;
            }
         }
      }
   }

   public synchronized void delete() throws ResourceException
   {
      delete(cqcFactory.getHandler());
   }

   public synchronized void delete(ResponseHandler responseHandler) throws ResourceException
   {
      Request request = new Request( Method.DELETE, new Reference( reference.toUri() ).toString() );
      cqcFactory.updateCommandRequest( request );

      int tries = 3;
      while (true)
      {
         Response response = new Response( request );
         try
         {
            cqcFactory.getClient().handle( request, response );
            if (!response.getStatus().isSuccess())
            {
               handleError( response );
            } else
            {
               // Reset modification date
               cqcFactory.updateCache( response );

               responseHandler.handleResponse( response );
            }

            break;
         } catch (ResourceException e)
         {
            if (e.getStatus().equals( Status.CONNECTOR_ERROR_COMMUNICATION ) ||
                  e.getStatus().equals( Status.CONNECTOR_ERROR_CONNECTION ))
            {
               if (tries == 0)
                  throw e; // Give up
               else
               {
                  // Try again
                  tries--;
                  continue;
               }
            } else
            {
               // Abort
               throw e;
            }
         } finally
         {
            response.release();
         }
      }
   }

   public synchronized CommandQueryClient getSubClient( String pathSegment )
   {
      Reference subReference = reference.clone().addSegment( pathSegment ).addSegment( "" );
      return cqcFactory.newClient(subReference);
   }

   public synchronized CommandQueryClient getClient( String relativePath )
   {
      Reference reference = this.reference.clone();
      if (relativePath.startsWith( "/" ))
         reference.setPath( relativePath );
      else
      {
         reference.setPath( reference.getPath() + relativePath );
         reference = reference.normalize();
      }

      return cqcFactory.newClient( reference );
   }

   public synchronized CommandQueryClient getClient( LinkValue link )
   {
      return getClient( link.href().get() );
   }
}