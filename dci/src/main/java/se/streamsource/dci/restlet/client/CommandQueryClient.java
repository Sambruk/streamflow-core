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
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.ContextValue;
import se.streamsource.dci.value.LinkValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for client-side Command/Query resources
 */
public class CommandQueryClient
{
   private static final Set<Tag> validTags = new HashSet<Tag>( );

   @Structure
   private ValueBuilderFactory vbf;

   @Structure
   private ObjectBuilderFactory obf;

   @Structure
   private Qi4jSPI spi;

   @Uses
   protected ResponseHandler responseHandler;

   @Uses
   private Uniform client;

   @Uses
   private Reference reference;

   private Date lastModified; // Keep track of last-modified on queries, and send it on commands
   private Tag tag; // Keep track of E-Tag, to detect when someone else has issued a command to the same resource

   public Reference getReference()
   {
      return reference;
   }

   public <T extends ValueComposite> T query( String operation, Class<T> queryResult ) throws ResourceException
   {
      return query( operation, null, queryResult );
   }

   public <T extends ValueComposite> T query( String operation, ValueComposite queryValue, Class<T> queryResult ) throws ResourceException
   {
      Response response = invokeQuery( operation, queryValue );

      if (response.getStatus().isSuccess())
      {
         saveTagTimeStamp(response);

         String jsonValue = response.getEntityAsText();

         T returnValue = vbf.newValueFromJSON( queryResult, jsonValue );
         return returnValue;
      } else
      {
         // This will throw an exception
         handleError( response );
         return null;
      }
   }

   private void checkTag()
   {
      // Check if we need to refresh first
      if (!validTags.contains( tag ))
      {
         // This will update the lastModified timestamp + tag before issuing the command
         query( "", ContextValue.class );
      }
   }

   private void saveTagTimeStamp( Response response )
   {
      Tag tag = response.getEntity().getTag();
      if (tag != null)
      {
         lastModified = response.getEntity().getModificationDate();
         this.tag = tag;
         validTags.add( tag );
      }
   }

   public InputStream queryStream( String operation, ValueComposite queryValue ) throws ResourceException, IOException
   {
      Response response = invokeQuery( operation, queryValue );

      if (response.getStatus().isSuccess())
      {
         return response.getEntity().getStream();
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
      StateHolder holder = spi.getState( queryValue );
      final ValueDescriptor descriptor = spi.getValueDescriptor( queryValue );

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

   public void postLink( LinkValue link ) throws ResourceException
   {
      postCommand( link.href().get(), new EmptyRepresentation() );
   }

   public void postCommand( String operation ) throws ResourceException
   {
      postCommand( operation, new EmptyRepresentation() );
   }

   public void postCommand( String operation, ValueComposite command ) throws ResourceException
   {
      Representation commandRepresentation;
      commandRepresentation = new StringRepresentation( command.toJSON(), MediaType.APPLICATION_JSON, null, CharacterSet.UTF_8 );

      postCommand( operation, commandRepresentation );
   }

   public void postCommand( String operation, Representation commandRepresentation )
         throws ResourceException
   {
      postCommand( operation, commandRepresentation, responseHandler );
   }

   public void postCommand( String operation, Representation commandRepresentation, ResponseHandler responseHandler )
         throws ResourceException
   {
      checkTag();

      Reference ref = new Reference( reference.toUri().toString() + operation );
      Request request = new Request( Method.POST, ref, commandRepresentation );
      ClientInfo info = new ClientInfo();
      info.setAgent( "Streamflow" ); // TODO Set versions correctly
      info.setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( MediaType.APPLICATION_JSON ) ) );
      request.setClientInfo( info );
      request.getConditions().setUnmodifiedSince( lastModified );

      Response response = new Response( request );
      client.handle( request, response );

      try
      {
         if (response.getStatus().isSuccess())
         {
            if (tag != null)
               validTags.remove( tag );

            saveTagTimeStamp( response );
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
            throw new ResourceException( response.getStatus(), text );
         } else
         {
            throw new ResourceException( response.getStatus() );
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
      ClientInfo info = new ClientInfo();
      info.setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( MediaType.APPLICATION_JSON ) ) );
      request.setClientInfo( info );

      Response response = new Response( request );

      client.handle( request, response );

      return response;
   }

   public void create() throws ResourceException
   {
      putCommand( null );
   }

   public void putCommand( String operation ) throws ResourceException
   {
      putCommand( operation, null );
   }

   public void putCommand( String operation, ValueComposite command ) throws ResourceException
   {
      checkTag();

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
      ClientInfo info = new ClientInfo();
      info.setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( MediaType.APPLICATION_JSON ) ) );
      request.setClientInfo( info );
      request.getConditions().setUnmodifiedSince( lastModified );
      request.setEntity( commandRepresentation );
      int tries = 3;
      while (true)
      {
         try
         {
            Response response = new Response( request );
            client.handle( request, response );

            try
            {
               if (response.getStatus().isSuccess())
               {
                  if (tag != null)
                     validTags.remove( tag );

                  // Reset modification date
                  saveTagTimeStamp( response );

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

   public void delete() throws ResourceException
   {
      checkTag();

      Request request = new Request( Method.DELETE, new Reference( reference.toUri() ).toString() );
      ClientInfo info = new ClientInfo();
      info.setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( MediaType.APPLICATION_JSON ) ) );
      request.setClientInfo( info );

      int tries = 3;
      while (true)
      {
         Response response = new Response(request);
         try
         {
            client.handle(request, response);
            if (!response.getStatus().isSuccess())
            {
               handleError( response );
            } else
            {
               // Reset modification date
               if (tag != null)
               {
                  lastModified = null;
                  validTags.remove( tag );
                  tag = null;
               }

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

   public CommandQueryClient getSubClient( String pathSegment )
   {
      Reference subReference = reference.clone().addSegment( pathSegment ).addSegment( "" );
      return obf.newObjectBuilder( getClass() ).use( client, new Context(), subReference, responseHandler ).newInstance();
   }

   public CommandQueryClient getClient( String relativePath )
   {
      Reference reference = this.reference.clone();
      if (relativePath.startsWith( "/" ))
         reference.setPath( relativePath );
      else
      {
         reference.setPath( reference.getPath()+relativePath );
         reference = reference.normalize();
      }

      return obf.newObjectBuilder( getClass() ).use( client, new Context(), reference, responseHandler ).newInstance();
   }

   public CommandQueryClient getClient( LinkValue link )
   {
      return getClient( link.href().get() );
   }
}