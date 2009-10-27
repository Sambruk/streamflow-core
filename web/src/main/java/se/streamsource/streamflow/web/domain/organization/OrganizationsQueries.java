/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
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

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.user.UserEntityDTO;
import se.streamsource.streamflow.resource.user.UserEntityListDTO;
import se.streamsource.streamflow.web.domain.user.User;
import se.streamsource.streamflow.web.domain.user.UserEntity;

import java.util.List;

@Mixins(OrganizationsQueries.OrganizationsQueriesMixin.class)
public interface OrganizationsQueries
{
    public ListValue organizations();

    public UserEntityListDTO users();

    class OrganizationsQueriesMixin
        implements OrganizationsQueries
    {
        @Structure
        ValueBuilderFactory vbf;

        @Structure
        QueryBuilderFactory qbf;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        OrganizationsEntity.OrganizationsState state;

        public ListValue organizations()
        {
            return new ListValueBuilder(vbf).addDescribableItems( state.getAllOrganizations() ).newList();
        }

        public UserEntityListDTO users()
        {
            Query<UserEntity> usersQuery = qbf.newQueryBuilder(UserEntity.class).
                    newQuery(uowf.currentUnitOfWork());

            usersQuery.orderBy(orderBy(templateFor(User.UserState.class).userName()));


            ValueBuilder<UserEntityListDTO> listBuilder = vbf.newValueBuilder(UserEntityListDTO.class);
            List<UserEntityDTO> userlist = listBuilder.prototype().users().get();

            ValueBuilder<UserEntityDTO> builder = vbf.newValueBuilder(UserEntityDTO.class);

            for(UserEntity user : usersQuery)
            {
                builder.prototype().entity().set(EntityReference.getEntityReference(user));
                builder.prototype().username().set(user.userName().get());
                builder.prototype().disabled().set(user.disabled().get());

                userlist.add(builder.newInstance());
            }

            return listBuilder.newInstance();

        }
    }
}
