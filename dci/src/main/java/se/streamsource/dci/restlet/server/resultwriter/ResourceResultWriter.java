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

package se.streamsource.dci.restlet.server.resultwriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueComposite;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.server.velocity.ValueCompositeContext;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.Links;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * ResultWriter for ResourceValues
 */
public class ResourceResultWriter
   extends AbstractResultWriter
{
   private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM );
   private Template htmlTemplate;

   public ResourceResultWriter(@Service VelocityEngine velocity) throws Exception
   {
      htmlTemplate = velocity.getTemplate( "rest/template/resource.htm" );
   }

   public boolean write( final Object result, final Response response ) throws ResourceException
   {
      if (result instanceof ResourceValue)
      {
         ResourceValue resourceValue = (ResourceValue) result;

         // Allowed methods
         response.getAllowedMethods().add( Method.GET );
         if (Iterables.matchesAny( Links.withRel( "delete" ), resourceValue.commands().get() ))
            response.getAllowedMethods().add( Method.DELETE );
         if (Iterables.matchesAny( Links.withRel( "update" ), resourceValue.commands().get()))
            response.getAllowedMethods().add( Method.PUT );

         // Response according to what client accepts
         MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes ).getMediaType();
         if (MediaType.APPLICATION_JSON.equals(type))
         {
            response.setEntity( new StringRepresentation( resourceValue.toJSON(), MediaType.APPLICATION_JSON));
            return true;
         } else if (MediaType.TEXT_HTML.equals(type))
         {
            Representation rep = new WriterRepresentation( MediaType.TEXT_HTML )
            {
               @Override
               public void write( Writer writer ) throws IOException
               {
                  VelocityContext context = new VelocityContext();
                  context.put( "request", response.getRequest() );
                  context.put( "response", response );

                  context.put( "result", new ValueCompositeContext((ValueComposite) result) );
                  htmlTemplate.merge( context, writer );
               }
            };
            response.setEntity( rep );
            return true;
         }
      }

      return false;
   }
}
