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

package se.streamsource.streamflow.web.rest.resource.surface.accesspoints.endusers;

import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.streamflow.web.application.security.UserPrincipal;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.EndUsersContext;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;

/**
 * JAVADOC
 */
public class EndUsersResource
      extends CommandQueryResource
      implements SubResources
{
   public EndUsersResource()
   {
      super( EndUsersContext.class );
   }

   public void resource( String segment ) throws ResourceException
   {
      ProxyUser proxyUser = RoleMap.role(ProxyUser.class);
      try
      {
         RoleMap.current().set(proxyUser.getEndUser(segment));
      } catch (NoSuchEntityException e)
      {
         throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
      }
      RoleMap.current().set( new UserPrincipal( segment ) );

      subResource( EndUserResource.class );
   }
}