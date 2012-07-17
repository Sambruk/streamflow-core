/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.rest.resource;

import se.streamsource.dci.api.Requires;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.streamflow.web.context.RequiresPermission;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.rest.resource.account.AccountResource;
import se.streamsource.streamflow.web.rest.resource.administration.AdministrationResource;
import se.streamsource.streamflow.web.rest.resource.contactlookup.ContactLookupResource;
import se.streamsource.streamflow.web.rest.resource.crystal.CrystalResource;
import se.streamsource.streamflow.web.rest.resource.overview.OverviewResource;
import se.streamsource.streamflow.web.rest.resource.surface.SurfaceResource;
import se.streamsource.streamflow.web.rest.resource.workspace.WorkspaceResource;

/**
 * Root of Streamflow REST API
 */
public class RootResource
      extends CommandQueryResource
{
   @Requires(OrganizationParticipations.class)
   @SubResource
   public void account()
   {
      subResource( AccountResource.class );
   }

   @Requires(OrganizationParticipations.class)
   @SubResource
   public void workspace()
   {
      subResource( WorkspaceResource.class );
   }

   @Requires(OrganizationParticipations.class)
   @SubResource
   public void overview()
   {
      subResource( OverviewResource.class );
   }

   @Requires(OrganizationParticipations.class)
   @SubResource
   public void administration()
   {
      setRole(UsersEntity.class, UsersEntity.USERS_ID);
      subResource( AdministrationResource.class );
   }

   @Requires(ProxyUser.class)
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

   @Requires(ProxyUser.class)
   @SubResource
   public void contactlookup()
   {
      subResource(  ContactLookupResource.class );
   }
}
