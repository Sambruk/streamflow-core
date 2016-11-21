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
package se.streamsource.dci.restlet.server.responsewriter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.qi4j.library.constraints.annotation.InstanceOf;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import java.util.Arrays;
import java.util.List;

/**
 * Handles JSONObject output
 */
public class JSONResponseWriter
      extends AbstractResponseWriter
{
   private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON );

   public boolean write( final Object result, final Response response ) throws ResourceException
   {
      if (result instanceof JSONObject || result instanceof JSONArray)
      {
         MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes).getMediaType();
         
         String jsonValue;
         if (result instanceof JSONObject)
         {
            jsonValue = ((JSONObject) result).toString();
         } else
         {
            jsonValue = ((JSONArray) result).toString();
         }
         
         if (MediaType.APPLICATION_JSON.equals(type))
         {
            
            StringRepresentation representation = new StringRepresentation( jsonValue,
                  MediaType.APPLICATION_JSON );

            response.setEntity( representation );

            return true;
         } else if (MediaType.TEXT_HTML.equals(type))
         {
            StringRepresentation representation = new StringRepresentation( jsonValue,
                  MediaType.TEXT_HTML );

            response.setEntity( representation );

            return true;
         }
      }

      return false;
   }
}
