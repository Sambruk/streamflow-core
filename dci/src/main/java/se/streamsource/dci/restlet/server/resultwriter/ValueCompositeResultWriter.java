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

package se.streamsource.dci.restlet.server.resultwriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueComposite;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.server.ResultWriter;
import se.streamsource.dci.restlet.server.velocity.ValueCompositeContext;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * JAVADOC
 */
public class ValueCompositeResultWriter
      implements ResultWriter
{
   private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.APPLICATION_JSON, MediaType.TEXT_HTML );
   private Template valueHtmlTemplate;
   private final VelocityEngine velocity;

   public ValueCompositeResultWriter( @Service VelocityEngine velocity ) throws Exception
   {
      this.velocity = velocity;
      valueHtmlTemplate = velocity.getTemplate( "rest/template/value.htm" );
   }

   public boolean write( final Object result, final Response response ) throws ResourceException
   {
      if (result instanceof Value)
      {
         MediaType type = response.getRequest().getClientInfo().getPreferredMediaType( supportedMediaTypes );
         if (type.equals( MediaType.APPLICATION_JSON ))
         {
            StringRepresentation representation = new StringRepresentation( ((Value) result).toJSON(),
                  MediaType.APPLICATION_JSON );

            response.setEntity( representation );
            return true;
         } else if (type.equals( MediaType.TEXT_HTML ))
         {
            // Look for type specific template
            Template template;
            try
            {
               template = velocity.getTemplate( "rest/template/" + result.getClass().getInterfaces()[0].getSimpleName() + ".htm" );
            } catch (Exception e)
            {
               // Use default
               template = valueHtmlTemplate;
            }


            final Template finalTemplate = template;
            Representation rep = new WriterRepresentation( MediaType.TEXT_HTML )
            {
               @Override
               public void write( Writer writer ) throws IOException
               {
                  VelocityContext context = new VelocityContext();
                  context.put( "request", response.getRequest() );
                  context.put( "response", response );

                  context.put( "result", new ValueCompositeContext( (ValueComposite) result) );
                  finalTemplate.merge( context, writer );
               }
            };
            response.setEntity( rep );
            return true;
         }
      }

      return false;
   }
}