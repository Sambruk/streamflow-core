/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.resource;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.routing.Filter;
import org.restlet.service.MetadataService;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * JAVADOC
 */
public class ViewFilter
      extends Filter
{
   VelocityEngine velocity = new VelocityEngine();

   public ViewFilter( @Uses Context context, @Uses Restlet next ) throws Exception
   {
      super( context, next );

      velocity.init( getClass().getResource( "/velocity.properties" ).getPath() );
   }

   @Override
   protected int doHandle( final Request request, final Response response )
   {
      MediaType responseType = request.getClientInfo().getPreferredMediaType( Arrays.asList( MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML ) );
      if (responseType != null && responseType.equals( MediaType.TEXT_HTML ))
      {
         List<Preference<MediaType>> mediaTypes = request.getClientInfo().getAcceptedMediaTypes();
         request.getClientInfo().setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( MediaType.APPLICATION_JSON ) ) );

         int result = super.doHandle( request, response );

         request.getClientInfo().setAcceptedMediaTypes( mediaTypes );

         MetadataService metadataService = getApplication().getMetadataService();
         final String extension = metadataService.getExtension( responseType );

         // Check if 404
         if (response.getStatus().equals( Status.CLIENT_ERROR_NOT_FOUND ))
         {
            // Check if template exists
            String templateName = "rest" + request.getResourceRef().getRemainingPart() + "."+extension;
            try
            {
               final Template template = velocity.getTemplate( templateName );

               response.setEntity( new WriterRepresentation( MediaType.TEXT_HTML )
               {
                  @Override
                  public void write( Writer writer ) throws IOException
                  {
                     template.merge( new VelocityContext(), writer );
                  }
               } );

            } catch (Exception e)
            {
               // Do nothing
               return result;
            }

            return result;
         }

         if (response.getEntity() != null && !(response.getEntity() instanceof EmptyRepresentation) && response.getEntity().getMediaType().equals( MediaType.APPLICATION_JSON ))
         {
            try
            {
               final Reader reader = response.getEntity().getReader();
               response.setEntity( new WriterRepresentation( MediaType.TEXT_HTML )
               {
                  @Override
                  public void write( Writer writer ) throws IOException
                  {
                     try
                     {
                        JSONTokener tokener = new JSONTokener( reader );
                        final JSONObject object = (JSONObject) tokener.nextValue();

                        Template template = resolveTemplate( new File( templateName(response) ) );

                        if (template != null)
                        {
                           VelocityContext context = new VelocityContext();
                           context.put( "request", request );
                           context.put( "response", response );
                           context.put( "result", new JSONObjectContext( object ) );
                           template.merge( context, writer );
                        } else
                        {
                           if (object.opt( "links" ) != null)
                           {
                              template = velocity.getTemplate( "se/streamsource/streamflow/web/resource/resources/links.html" );
                              VelocityContext context = new VelocityContext( new JSONObjectContext( object ) );
                              context.put( "request", request );
                              context.put( "response", response );
                              context.put( "result", object.toString() );
                              template.merge( context, writer );
                           } else
                              writer.write( object.toString() );
                        }
                     } catch (Exception e)
                     {
                        throw (IOException) new IOException().initCause( e );
                     }
                  }

                  private String templateName( Response response )
                  {
                     List<String> segments = (List<String>) response.getAttributes().get( "segments" );

                     String templateName = "";
                     for (String segment : segments)
                     {
                        if (!(segment.equals( "" ) || segment.equals(".")))
                           templateName += "/" + segment;
                        else
                           templateName += "/context";
                     }
                     templateName += "."+extension;

                     return templateName;
                  }
               } );

               return result;
            } catch (Exception e)
            {
               return result;
            }
         } else
            return result;
      } else
      {
         return super.doHandle( request, response );
      }
   }

   private Template resolveTemplate( File templateName ) throws Exception
   {

      Template template = null;
      do
      {
         try
         {
            template = velocity.getTemplate( new File( "rest", templateName.toString() ).toString() );
         } catch (ResourceNotFoundException e)
         {
            File parentFile = templateName.getParentFile();
            if (parentFile.toString().equals("/"))
               return null;

            templateName = new File( parentFile.getParentFile(), templateName.getName() );
         }

      } while (template == null);

      return template;
   }

   static class JSONObjectContext
         extends AbstractContext
   {
      JSONObject object;

      JSONObjectContext( JSONObject object )
      {
         this.object = object;
      }

      @Override
      public Object internalGet( String s )
      {
         Object result = object.opt( s );

         if (result instanceof JSONObject)
         {
            result = new JSONObjectContext( (JSONObject) result );
         } else if (result instanceof JSONArray)
         {
            result = new JSONArrayContext( (JSONArray) result );
         }

         return result;
      }

      @Override
      public Object internalPut( String s, Object o )
      {
         try
         {
            return object.put( s, o );
         } catch (JSONException e)
         {
            throw new RuntimeException( e );
         }
      }

      @Override
      public boolean internalContainsKey( Object o )
      {
         return object.has( o.toString() );
      }

      @Override
      public Object[] internalGetKeys()
      {
         List keys = new ArrayList();

         Iterator iterator = object.keys();
         while (iterator.hasNext())
         {
            keys.add( iterator.next() );

         }

         return keys.toArray();
      }

      @Override
      public Object internalRemove( Object o )
      {
         return object.remove( o.toString() );
      }

      @Override
      public String toString()
      {
         return object.toString();
      }
   }

   public static class JSONArrayContext
         extends AbstractContext
         implements Iterable
   {
      JSONArray array;

      JSONArrayContext( JSONArray array )
      {
         this.array = array;
      }

      @Override
      public Object internalGet( String s )
      {
         Object result = array.opt( Integer.parseInt( s ) );

         if (result instanceof JSONObject)
         {
            result = new JSONObjectContext( (JSONObject) result );
         } else if (result instanceof JSONArray)
         {
            result = new JSONArrayContext( (JSONArray) result );
         }

         return result;
      }

      @Override
      public Object internalPut( String s, Object o )
      {
         try
         {
            return array.put( Integer.parseInt( s ), o );
         } catch (JSONException e)
         {
            throw new RuntimeException( e );
         }
      }

      @Override
      public boolean internalContainsKey( Object o )
      {
         return !array.isNull( Integer.parseInt( o.toString() ) );
      }

      @Override
      public Object[] internalGetKeys()
      {
         String[] indices = new String[array.length()];
         for (int i = 0; i < indices.length; i++)
         {
            indices[i] = Integer.toString( i );
         }

         return indices;
      }

      @Override
      public Object internalRemove( Object o )
      {
         return array.remove( Integer.parseInt( o.toString() ) );
      }

      @Override
      public String toString()
      {
         return array.toString();
      }

      public Iterator iterator()
      {
         return new Iterator()
         {
            int idx = 0;

            public boolean hasNext()
            {
               return idx < array.length();
            }

            public Object next()
            {
               try
               {
                  Object result = array.get( idx++ );

                  if (result instanceof JSONObject)
                  {
                     result = new JSONObjectContext( (JSONObject) result );
                  } else if (result instanceof JSONArray)
                  {
                     result = new JSONArrayContext( (JSONArray) result );
                  }

                  return result;
               } catch (JSONException e)
               {
                  return null;
               }
            }

            public void remove()
            {
            }
         };
      }
   }
}
