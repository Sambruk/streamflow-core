/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.entity.organization;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.web.domain.interaction.authentication.Authentication;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;

import java.security.AllPermission;
import java.security.PermissionCollection;

/**
 * Ask an entity to compute the set of permissions for a particular user
 */
@Mixins(PermissionQueries.Mixin.class)
public interface PermissionQueries
{
   PermissionCollection getPermissions( Authentication user );

   class Mixin
      implements PermissionQueries
   {
      @This RolePolicy.Data data;

      public PermissionCollection getPermissions( Authentication user )
      {
         PermissionCollection permissions = null;

         // If participant has any role, it's the Admin role -> AllPermissions
         if (data.hasRoles( (Participant) user ))
         {
            permissions = new AllPermission().newPermissionCollection();
            permissions.add( new AllPermission() );
         }

         return permissions;
      }
   }
}
