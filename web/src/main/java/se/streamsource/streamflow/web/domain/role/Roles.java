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

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.organization.DuplicateDescriptionException;
import se.streamsource.streamflow.web.domain.project.ProjectRole;
import se.streamsource.streamflow.web.domain.project.ProjectRoleEntity;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(Roles.RolesMixin.class)
public interface Roles
{
    Role createRole(String name) throws DuplicateDescriptionException;

    void removeRole(Role projectRole);

    boolean hasRole(Role projectRole);

    List<Role> getRoles();

    interface RolesState
    {
        @Aggregated
        ManyAssociation<Role> roles();
    }

    class RolesMixin
            implements Roles
    {
        @This
        RolesState state;

        @Structure
        UnitOfWorkFactory uowf;

        @Service
        IdentityGenerator idGen;

        public Role createRole(String name) throws DuplicateDescriptionException
        {
            for(Role arole : state.roles() )
            {
                if(arole.hasDescription(name))
                {
                    throw new DuplicateDescriptionException();
                }
            }

            // Create role
//            roleCreated(DomainEvent.CREATE, idGen.)
            Role role = uowf.currentUnitOfWork().newEntity(RoleEntity.class);
            role.describe(name);

            state.roles().add(state.roles().count(), role);

            return role;
        }

        public void removeRole(Role projectRole)
        {
            if (state.roles().remove(projectRole))
            {
                projectRole.removeEntity();
            }
        }

        public boolean hasRole(Role projectRole)
        {
            return state.roles().contains(projectRole);
        }

        public List<Role> getRoles()
        {
            return state.roles().toList();
        }
    }

}