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

package se.streamsource.dci.restlet.server.sitemesh;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.WriterRepresentation;
import org.restlet.routing.Filter;
import org.sitemesh.DecoratorSelector;
import org.sitemesh.content.Content;
import org.sitemesh.content.ContentProcessor;

import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;

/**
 * JAVADOC
 */
public class SiteMeshRestletFilter
   extends Filter
{
   private ContentProcessor contentProcessor;
   private DecoratorSelector<RestletContext> decoratorSelector;

   public SiteMeshRestletFilter( Context context,
                                 Restlet next,
                                 ContentProcessor contentProcessor,
                                 DecoratorSelector<RestletContext> decoratorSelector )
   {
      super( context, next );
      this.contentProcessor = contentProcessor;
      this.decoratorSelector = decoratorSelector;
   }

   @Override
   protected int doHandle( Request request, Response response )
   {
      int result = super.doHandle( request, response );

      RestletContext context = new RestletContext(getContext(), request, contentProcessor);
      
      if (!response.getStatus().isSuccess() || !response.getEntity().getMediaType().equals( MediaType.TEXT_HTML))
         return result;

      String rep = response.getEntityAsText();

      CharBuffer buffer = CharBuffer.wrap( rep );

      try
      {
         Content content = contentProcessor.build(buffer, context);
         if (content == null)
         {
             return result;
         }

         String[] decoratorPaths = decoratorSelector.selectDecoratorPaths(content, context);
         for (String decoratorPath : decoratorPaths) {
             content = context.decorate(decoratorPath, content);
         }

         if (content == null) {
             return result;
         }

         final Content finalContent = content;

         response.setEntity(new WriterRepresentation(response.getEntity().getMediaType())
         {
            @Override
            public void write( Writer writer ) throws IOException
            {
               finalContent.getData().writeValueTo(writer);
            }
         });
      } catch (IOException e)
      {
         response.setStatus( Status.SERVER_ERROR_INTERNAL, e );
      }

      return result;
   }
}
