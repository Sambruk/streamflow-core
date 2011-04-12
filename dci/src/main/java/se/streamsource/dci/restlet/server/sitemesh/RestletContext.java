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

import org.restlet.*;
import org.restlet.data.*;
import org.sitemesh.*;
import org.sitemesh.content.*;

import java.io.*;

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
