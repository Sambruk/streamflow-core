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
package se.streamsource.dci.restlet.server;

import org.restlet.Response;
import org.restlet.resource.ResourceException;

/**
 * Writes a result object to a response.
 */
public interface ResponseWriter
{
   /**
    * Write the given result object to the response.
    *
    * @param result the result of the invocation
    * @param response the response for this request
    * @return true if result was written, false if this writer does not know how to handle it
    * @throws ResourceException if the writer attempted to write the result but failed
    */
   public boolean write(Object result, Response response)
      throws ResourceException;
}
