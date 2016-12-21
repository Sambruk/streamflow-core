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
package se.streamsource.streamflow.server.plugin.restlet;

import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.server.plugin.ldapimport.GroupListValue;
import se.streamsource.streamflow.server.plugin.ldapimport.LdapImporter;
import se.streamsource.streamflow.server.plugin.ldapimport.UserListValue;

/**
 * Empty restlet...
 */
public class LdapImporterRestlet extends Restlet
{
   @Structure
   Module module;

   @Optional
   @Service
   LdapImporter importer;

   @Override
   public void handle(Request request, Response response)
   {
      super.handle(request, response);

      try
      {
         if (request.getMethod().equals(Method.GET))
         {

            try
            {
               if ("users".equalsIgnoreCase(request.getResourceRef().getLastSegment()))
               {
                  UserListValue users = importer.users();
                  StringRepresentation result = new StringRepresentation(users.toJSON(),
                        MediaType.APPLICATION_JSON, Language.DEFAULT, CharacterSet.UTF_8);
                  response.setStatus(Status.SUCCESS_OK);
                  response.setEntity(result);
               } else if ("groups".equalsIgnoreCase(request.getResourceRef().getLastSegment()))
               {
                  GroupListValue groups = importer.groups();
                  StringRepresentation result = new StringRepresentation(groups.toJSON(),
                        MediaType.APPLICATION_JSON, Language.DEFAULT, CharacterSet.UTF_8);
                  response.setStatus(Status.SUCCESS_OK);
                  response.setEntity(result);
               }
            } catch (ResourceException e)
            {
               response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
               response.setEntity(e.getStatus().getDescription(), MediaType.TEXT_PLAIN);
            }


         } else

         {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
         }
      } finally
      {
         request.release();
      }
   }
}
