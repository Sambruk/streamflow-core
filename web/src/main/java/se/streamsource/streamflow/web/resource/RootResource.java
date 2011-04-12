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

package se.streamsource.streamflow.web.resource;

import se.streamsource.dci.api.*;
import se.streamsource.dci.restlet.server.*;
import se.streamsource.dci.restlet.server.api.*;
import se.streamsource.streamflow.web.context.*;
import se.streamsource.streamflow.web.domain.entity.user.*;
import se.streamsource.streamflow.web.domain.interaction.security.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.user.*;
import se.streamsource.streamflow.web.resource.account.*;
import se.streamsource.streamflow.web.resource.administration.*;
import se.streamsource.streamflow.web.resource.crystal.*;
import se.streamsource.streamflow.web.resource.overview.*;
import se.streamsource.streamflow.web.resource.surface.*;
import se.streamsource.streamflow.web.resource.workspace.*;

/**
 * Root of Streamflow REST API
 */
public class RootResource
      extends CommandQueryResource
{
   @RequiresRoles(OrganizationParticipations.class)
   @SubResource
   public void account()
   {
      subResource( AccountResource.class );
   }

   @RequiresRoles(OrganizationParticipations.class)
   @SubResource
   public void workspace()
   {
      subResource( WorkspaceResource.class );
   }

   @RequiresRoles(OrganizationParticipations.class)
   @SubResource
   public void overview()
   {
      subResource( OverviewResource.class );
   }

   @RequiresRoles(OrganizationParticipations.class)
   @SubResource
   public void administration()
   {
      setRole(UsersEntity.class, UsersEntity.USERS_ID);
      subResource( AdministrationResource.class );
   }

   @RequiresRoles(ProxyUser.class)
   @SubResource
   public void surface()
   {
      setRole( UsersEntity.class, UsersEntity.USERS_ID );
      subResource( SurfaceResource.class );
   }

   @RequiresPermission(PermissionType.administrator)
   @SubResource
   public void crystal()
   {
      subResource( CrystalResource.class );
   }
}
