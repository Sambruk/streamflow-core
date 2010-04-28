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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.service.MetadataService;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.restlet.server.velocity.ValueCompositeContext;
import se.streamsource.dci.value.LinksValue;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * JAVADOC
 */
public class DefaultResponseWriterFactory
      implements ResponseWriterFactory
{
   VelocityEngine velocity;

   @Service
   MetadataService metadataService;

   private Template linksHtmlTemplate;
   private Template linksAtomTemplate;
   private Template formHtmlTemplate;
   private Template valueHtmlTemplate;

   public DefaultResponseWriterFactory( @Service VelocityEngine velocity ) throws Exception
   {
      this.velocity = velocity;

      URL inputStream = getClass().getResource( "/velocity.properties" );

      if (inputStream == null)
         throw new IllegalStateException("Could not find velocity.properties in classpath");

      Properties properties = new Properties();
      properties.load( inputStream.openStream() );

      velocity.init( properties );

      linksHtmlTemplate = velocity.getTemplate( "rest/template/links.htm" );
      linksAtomTemplate = velocity.getTemplate( "rest/template/links.atom" );
      formHtmlTemplate = velocity.getTemplate( "rest/template/form.htm" );
      valueHtmlTemplate = velocity.getTemplate( "rest/template/value.htm" );
   }

   public ResponseWriter createWriter( List<String> segments, Class resultType, Context context, Variant variant )
         throws Exception
   {
      if (Representation.class.isAssignableFrom( resultType ))
      {
         // Return representation as-is
         // TODO refactor this into its own factory
         return new RepresentationResponseWriter( variant );
      } else if (variant.getMediaType().equals( MediaType.APPLICATION_JSON ) && Value.class.isAssignableFrom( resultType ))
      {
         return new JsonResponseWriter( variant );
      } else
      {
         final String extension = metadataService.getExtension( variant.getMediaType() );

         String tn = templateName( segments, extension );
         Template template = resolveTemplate( new File( tn ) );

         if (template != null)
         {
            return new VelocityResponseWriter( template, context, variant );
         } else
         {
            // Check if links, then try default templates
            if (LinksValue.class.isAssignableFrom( resultType))
            {
               // Use standard links rendering templates
               if (variant.getMediaType().equals(MediaType.TEXT_HTML))
               {
                  return new VelocityResponseWriter( linksHtmlTemplate, context, variant );
               } else if (variant.getMediaType().equals( MediaType.APPLICATION_ATOM ))
               {
                  return new VelocityResponseWriter( linksAtomTemplate, context, variant );
               }
            } else if (ValueDescriptor.class.equals(resultType))
            {
               return new VelocityResponseWriter( formHtmlTemplate, context, variant );
            } else if (ValueComposite.class.isAssignableFrom( resultType ))
            {
               // Look for type specific template
               try
               {
                  template = velocity.getTemplate( "rest/template/"+resultType.getInterfaces()[0].getSimpleName()+"."+extension );
               } catch (ResourceNotFoundException e)
               {
                  template = valueHtmlTemplate;
               }

               return new VelocityResponseWriter( template, context, variant );
            }

            throw new IllegalArgumentException( "Cannot handle URL with this variant" );
         }
      }
   }

   private String templateName( List<String> segments, String extension )
   {
      String templateName = "";
      for (String segment : segments)
      {
         if (!(segment.equals( "" ) || segment.equals( "." )))
            templateName += "/" + segment;
         else
            templateName += "/context";
      }
      templateName += "." + extension;

      return templateName;
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
            if (parentFile.toString().equals( "/" ))
               return null;

            templateName = new File( parentFile.getParentFile(), templateName.getName() );
         }

      } while (template == null);

      return template;
   }

   private class VelocityResponseWriter implements ResponseWriter
   {
      private Template template;
      private Context context;
      private Variant variant;

      public VelocityResponseWriter( Template template, Context context, Variant variant )
      {
         this.template = template;
         this.context = context;
         this.variant = variant;
      }

      public void write( final Object result, final Request request, final Response response )
      {
         Representation rep = new WriterRepresentation( variant.getMediaType() )
         {
            @Override
            public void write( Writer writer ) throws IOException
            {
               VelocityContext context = new VelocityContext();
               context.put( "request", request );
               context.put( "response", response );
               context.put( "context", VelocityResponseWriter.this.context );

               Object contextResult = result;

               if (contextResult instanceof Value)
                  contextResult = new ValueCompositeContext((ValueComposite) contextResult);

               context.put( "result", contextResult );
               template.merge( context, writer );
            }
         };
         rep.setCharacterSet( variant.getCharacterSet() );
         rep.setLanguages( variant.getLanguages() );

         response.setStatus( Status.SUCCESS_OK );
         response.setEntity( rep );
      }
   }

   private class RepresentationResponseWriter implements ResponseWriter
   {
      private Variant variant;

      public RepresentationResponseWriter( Variant variant )
      {
         this.variant = variant;
      }

      public void write( Object result, Request request, Response response ) throws ResourceException
      {
         Representation rep = (Representation) result;

         // Ignore media type - interaction has already chosen!
         response.setStatus( Status.SUCCESS_OK );
         response.setEntity( rep );
      }
   }

   private class JsonResponseWriter implements ResponseWriter
   {
      private Variant variant;

      public JsonResponseWriter( Variant variant )
      {
         this.variant = variant;
      }

      public void write( Object result, Request request, Response response ) throws ResourceException
      {
         response.setEntity( new StringRepresentation( ((Value) result).toJSON(),
               MediaType.APPLICATION_JSON,
               variant.getLanguages().get( 0 ),
               variant.getCharacterSet() ));
         response.setStatus( Status.SUCCESS_OK );
      }
   }
}
