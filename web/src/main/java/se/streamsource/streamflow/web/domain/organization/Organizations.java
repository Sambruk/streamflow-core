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
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.domain.user.Password;
import se.streamsource.streamflow.domain.user.Username;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import static se.streamsource.streamflow.infrastructure.event.DomainEvent.*;
import se.streamsource.streamflow.web.domain.user.UserAuthentication;
import se.streamsource.streamflow.web.domain.user.UserEntity;

/**
 * JAVADOC
 */
@Mixins(Organizations.Mixin.class)
public interface Organizations
{
    OrganizationEntity createOrganization(String name);

    /**
     * Create user with the given password. Username has a constraint that allows the
     * username only to be a whole word, because it will be used as part of the REST url.
     *
     * @param username of the new user
     * @param password of the new user
     * @return the created user
     * @throws IllegalArgumentException if user with given name already exists
     */
    UserEntity createUser( @Username String username, @Password String password)
            throws IllegalArgumentException;

    interface Data
    {
        OrganizationEntity createdOrganization(DomainEvent event, String id);

        UserEntity createdUser(DomainEvent event, String username, String password);

        OrganizationEntity getOrganizationByName(String name);

        Query<OrganizationEntity> getAllOrganizations();

        UserEntity getUserByName(String name);
    }

    abstract class Mixin
            implements Organizations, Data
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Service
        IdentityGenerator idGen;

        @Structure
        QueryBuilderFactory qbf;

        @Structure
        ValueBuilderFactory vbf;

        public OrganizationEntity createOrganization(String name)
        {
//            OrganizationEntity ou = state.organizationCreated(CREATE, idGen.generate(OrganizationEntity.class));
            OrganizationEntity ou = createdOrganization(CREATE, "Organization");

            // Change name
            ou.changeDescription(name);

            // Create Administrator role
            ou.createRole("Administrator");

            return ou;
        }

        public UserEntity createUser(String username, String password)
                throws IllegalArgumentException
        {
            // Check if user already exist
            try
            {
                uowf.currentUnitOfWork().get( UserAuthentication.class, username);

                throw new IllegalArgumentException("user_already_exists");
            } catch (NoSuchEntityException e)
            {
                // Ok!
            }

            UserEntity user = createdUser(CREATE, username, password);
            return user;
        }

        public OrganizationEntity createdOrganization(DomainEvent event, String id)
        {
            return uowf.currentUnitOfWork().newEntity(OrganizationEntity.class, id);
        }

        public UserEntity createdUser(DomainEvent event, String username, String password)
        {
            EntityBuilder<UserEntity> builder = uowf.currentUnitOfWork().newEntityBuilder(UserEntity.class, username);
            UserEntity userEntity = builder.instance();
            userEntity.userName().set(username);
            userEntity.hashedPassword().set(userEntity.hashPassword(password));
            userEntity.contact().set(vbf.newValue(ContactValue.class));
            return builder.newInstance();
        }


        public OrganizationEntity getOrganizationByName(String name)
        {
            Describable.Data template = QueryExpressions.templateFor( Describable.Data.class);
            return qbf.newQueryBuilder(OrganizationEntity.class).
                    where(QueryExpressions.eq(template.description(), name)).
                    newQuery(uowf.currentUnitOfWork()).find();
        }

        public Query<OrganizationEntity> getAllOrganizations()
        {
            return qbf.newQueryBuilder(OrganizationEntity.class).
                    newQuery(uowf.currentUnitOfWork());
        }

        public UserEntity getUserByName(String name)
        {
            return uowf.currentUnitOfWork().get( UserEntity.class, name );
        }
    }
}
