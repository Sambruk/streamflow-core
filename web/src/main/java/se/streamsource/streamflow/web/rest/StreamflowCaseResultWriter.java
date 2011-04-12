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

package se.streamsource.streamflow.web.rest;

import org.qi4j.api.value.*;
import org.restlet.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import se.streamsource.dci.restlet.server.resultwriter.*;

import java.util.*;

/**
 * TODO
 */
public class StreamflowCaseResultWriter
        extends AbstractResultWriter
{
   private final List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
   private MediaType streamflowCaseMediatype;

   public StreamflowCaseResultWriter()
   {
      streamflowCaseMediatype = MediaType.register("application/x-streamflow-case+json", "Streamflow Case");
      supportedMediaTypes.add(streamflowCaseMediatype);
   }

   public boolean write(Object result, Response response) throws ResourceException
   {
      MediaType type = getVariant(response.getRequest(), ENGLISH, supportedMediaTypes).getMediaType();
      if (streamflowCaseMediatype.equals(type))
      {
         StringRepresentation representation = new StringRepresentation(((Value) result).toJSON(),
                 streamflowCaseMediatype);

         response.setEntity(representation);
         return true;
      }

      return false;
   }
}
