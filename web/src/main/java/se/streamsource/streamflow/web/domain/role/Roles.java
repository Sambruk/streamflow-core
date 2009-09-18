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

package se.streamsource.streamflow.web.domain.role;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Concerns(Roles.DescribeCreatedRoleConcern.class)
public interface Roles
{
    RoleEntity createRole(String name);
    void removeRole(RoleEntity projectRole);

    @Mixins(Roles.RolesMixin.class)
    interface RolesState
    {
        @Aggregated
        ManyAssociation<Role> roles();

        Role getAdministratorRole()
            throws IllegalStateException;

        RoleEntity roleCreated(DomainEvent event, String id);
        void roleRemoved(DomainEvent event, RoleEntity role);
    }

    public abstract class RolesMixin
        implements RolesState
    {
        public Role getAdministratorRole()
                throws IllegalStateException
        {
            if (roles().count() > 0)
                return roles().get(0);
            else
                throw new IllegalStateException("There's no Administrator role!");
        }
    }

    abstract class DescribeCreatedRoleConcern
        extends ConcernOf<Roles>
        implements Roles
    {
        public RoleEntity createRole(String name)
        {
            RoleEntity role = next.createRole(name);
            role.describe(name);
            return role;
        }
    }
}