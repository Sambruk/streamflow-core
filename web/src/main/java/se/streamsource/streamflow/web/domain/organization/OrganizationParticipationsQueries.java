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
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.web.domain.user.User;
import se.streamsource.streamflow.web.domain.user.UserEntity;

@Mixins(OrganizationParticipationsQueries.OrganizationParticipationsQueriesMixin.class)
public interface OrganizationParticipationsQueries
{
    public ListValue participatingUsers();

    class OrganizationParticipationsQueriesMixin
        implements OrganizationParticipationsQueries
    {
        @Structure
        QueryBuilderFactory qbf;

        @Structure
        ValueBuilderFactory vbf;

        @Structure
        UnitOfWorkFactory uowf;

        @This
        OrganizationEntity organization;

        public ListValue participatingUsers()
        {
            Query<UserEntity> usersQuery = qbf.newQueryBuilder(UserEntity.class).
                    newQuery(uowf.currentUnitOfWork());

            usersQuery.orderBy(orderBy(templateFor(User.UserState.class).userName()));

            ListValueBuilder userList = new ListValueBuilder(vbf);

            for (UserEntity entity : usersQuery)
            {
                if(entity.organizations().contains(organization))
                {
                    userList.addListItem(entity.getDescription(), EntityReference.getEntityReference(entity));
                }
            }

            return userList.newList();
        }
    }
}
