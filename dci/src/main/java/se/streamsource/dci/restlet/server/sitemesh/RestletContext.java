/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
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
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sitemesh.BaseSiteMeshContext;
import org.sitemesh.content.Content;
import org.sitemesh.content.ContentProcessor;

import java.io.IOException;
import java.io.Writer;

/**
 * JAVADOC
 */
public class RestletContext
   extends BaseSiteMeshContext
{
   private Context context;
   private Request request;

   public RestletContext( Context context, Request request, ContentProcessor contentProcessor )
   {
      super( contentProcessor );
      this.context = context;
      this.request = request;
   }

   @Override
   protected void decorate( String decoratorPath, Content content, Writer writer ) throws IOException
   {
      Response response;

      if (decoratorPath.startsWith( "riap:" ))
      {
         Request decorationRequest = new Request( Method.GET, decoratorPath);
         decorationRequest.setClientInfo( request.getClientInfo() );
         response = context.getServerDispatcher().handle(decorationRequest);
      } else
      {
         Request request = new Request(Method.GET, decoratorPath);
         response = context.getClientDispatcher().handle( request );
      }

      if (response.getStatus().equals( Status.CLIENT_ERROR_NOT_FOUND ))
      {
      }

      if (!response.getStatus().equals( Status.SUCCESS_OK ))
         throw new IOException("Could not process decorator "+decoratorPath+":"+ response.getStatus().getDescription());
      response.getEntity().write( writer );
   }

   public String getPath()
   {
      if (request.getResourceRef().getScheme().equals( "riap" ))
         return request.getResourceRef().getPath();
      else
         return request.getResourceRef().getRemainingPart();
   }
}
