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
package se.streamsource.dci.restlet.server.api;

import org.restlet.resource.ResourceException;

/**
 * Resources that have a collection of sub-resources should extends this interface.
 */
public interface SubResources
{
   /**
    * Instantiate the sub-resource, perform any RoleMap bindings based on
    * the given segment, and then call handle(Request,Response) on the subresource.
    *
    * @param segment the current segment that the sub-resource will correspond to
    * @throws org.restlet.resource.ResourceException if the subresource could not be lookup up, typically a 404
    */
   void resource( String segment )
      throws ResourceException;
}
