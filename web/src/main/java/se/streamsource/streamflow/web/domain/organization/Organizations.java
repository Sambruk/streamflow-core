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

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import static se.streamsource.streamflow.infrastructure.event.DomainEvent.CREATE;
import se.streamsource.streamflow.web.domain.user.User;
import se.streamsource.streamflow.web.domain.user.UserEntity;

/**
 * JAVADOC
 */
@Mixins(Organizations.OrganizationsMixin.class)
public interface Organizations
{
    Organization createOrganization(String name);

    User createUser(String username, String password);

    @Mixins(OrganisationsStateMixin.class)
    interface OrganizationsState
    {

        OrganizationEntity organizationCreated(DomainEvent event, String id);


        UserEntity userCreated(DomainEvent event, String username, String password);

        Organization findByName(String name);
    }

    class OrganizationsMixin
            implements Organizations
    {
        @Service
        IdentityGenerator idGen;

        @This
        OrganizationsState state;

        public Organization createOrganization(String name)
        {
//            OrganizationEntity ou = state.organizationCreated(CREATE, idGen.generate(OrganizationEntity.class));
            OrganizationEntity ou = state.organizationCreated(CREATE, "Organization");
            ou.describe(name);
            return ou;
        }

        public User createUser(String username, String password)
        {
            UserEntity user = state.userCreated(CREATE, username, password);
            return user;
        }
    }

    class OrganisationsStateMixin
            implements OrganizationsState
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        QueryBuilderFactory qbf;

        @Structure
        ValueBuilderFactory vbf;

        public OrganizationEntity organizationCreated(DomainEvent event, String id)
        {
            return uowf.currentUnitOfWork().newEntity(OrganizationEntity.class, id);
        }

        public UserEntity userCreated(DomainEvent event, String username, String password)
        {
            EntityBuilder<UserEntity> builder = uowf.currentUnitOfWork().newEntityBuilder(UserEntity.class, username);
            UserEntity userEntity = builder.instance();
            userEntity.userName().set(username);
            userEntity.hashedPassword().set(userEntity.hashPassword(password));
            userEntity.contact().set(vbf.newValue(ContactValue.class));
            return builder.newInstance();
        }


        public OrganizationEntity findByName(String name)
        {
            Describable.DescribableState template = QueryExpressions.templateFor(Describable.DescribableState.class);
            return qbf.newQueryBuilder(OrganizationEntity.class).
                    where(QueryExpressions.eq(template.description(), name)).
                    newQuery(uowf.currentUnitOfWork()).find();
        }
    }
}
