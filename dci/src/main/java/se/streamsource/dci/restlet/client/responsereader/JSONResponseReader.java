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

package se.streamsource.dci.restlet.client.responsereader;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import org.restlet.*;
import org.restlet.data.*;
import se.streamsource.dci.restlet.client.*;

/**
 * JAVADOC
 */
public class JSONResponseReader
   implements ResponseReader
{
   @Structure
   ValueBuilderFactory vbf;

   public <T> T readResponse( Response response, Class<T> resultType )
   {
      if (response.getEntity().getMediaType().equals( MediaType.APPLICATION_JSON) && ValueComposite.class.isAssignableFrom( resultType ))
      {
         String jsonValue = response.getEntityAsText();
         return vbf.newValueFromJSON( resultType, jsonValue );
      }

      return null;
   }
}
