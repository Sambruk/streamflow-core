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

package se.streamsource.streamflow.web.domain.organization;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.web.domain.DomainEntity;
import se.streamsource.streamflow.web.domain.role.UserPermissions;
import se.streamsource.streamflow.web.domain.user.UserAuthentication;

import java.security.AllPermission;
import java.security.PermissionCollection;

/**
 * JAVADOC
 */
@Mixins(OrganizationsEntity.UserPermissionsMixin.class)
public interface OrganizationsEntity
        extends Organizations,
        OrganizationsQueries,
        Organizations.Data,
        UserPermissions,
        DomainEntity
{
    public static final String ORGANIZATIONS_ID = "organizations";

    class UserPermissionsMixin
        implements UserPermissions
    {
        public PermissionCollection getPermissions( UserAuthentication user)
        {
            PermissionCollection permissions = null;

            // If user is administrator
            if (((UserAuthentication.Data)user).isAdministrator())
            {
                permissions = new AllPermission().newPermissionCollection();
                permissions.add(new AllPermission());
            }

            return permissions;
        }
    }
}