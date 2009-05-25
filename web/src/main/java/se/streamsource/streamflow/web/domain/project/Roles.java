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

package se.streamsource.streamflow.web.domain.project;

import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(Roles.RolesMixin.class)
public interface Roles
{
    void addRole(Role role);

    void removeRole(Role role);

    boolean hasRole(Role role);

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

        public void addRole(Role role)
        {
            state.roles().add(state.roles().count(), role);
        }

        public void removeRole(Role role)
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean hasRole(Role role)
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public List<Role> getRoles()
        {
            return state.roles().toList();
        }
    }

}
