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

package se.streamsource.streamflow.web.application.organization;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.api.usecase.UsecaseBuilder.*;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.web.domain.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.user.UserEntity;

/**
 * JAVADOC
 */
@Mixins(BootstrapDataService.TestDataMixin.class)
public interface BootstrapDataService
        extends ServiceComposite, Activatable
{
    class TestDataMixin
            implements Activatable
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        public void activate() throws Exception
        {
            UnitOfWork uow = uowf.newUnitOfWork(newUsecase("Bootstrap data"));

            try
            {
                // Check if admin users exists
                uow.get(UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME);
                uow.discard();
            } catch (NoSuchEntityException e)
            {
                // Create bootstrap data
                EntityBuilder<OrganizationEntity> ouBuilder = uow.newEntityBuilder(OrganizationEntity.class);
                ouBuilder.prototype().description().set("Organization");
                OrganizationEntity ou = ouBuilder.newInstance();

                // User
                UserEntity user = newUser(uow, ou);

                uow.complete();
            }

        }

        private UserEntity newUser(UnitOfWork uow, OrganizationEntity ou)
                throws UnitOfWorkCompletionException
        {
            // Create users
            EntityBuilder<UserEntity> userBuilder = uow.newEntityBuilder(UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME);
            UserEntity userState = userBuilder.prototype();
            userState.userName().set(UserEntity.ADMINISTRATOR_USERNAME);

            ValueBuilder<ContactValue> contactBuilder = vbf.newValueBuilder(ContactValue.class);
            contactBuilder.prototype().name().set("Administrator");
            ContactValue contact = contactBuilder.newInstance();
            userState.contact().set(contact);

            // Join the organizations.html
            userState.join(ou);

            return userBuilder.newInstance();
        }

        public void passivate() throws Exception
        {
        }
    }
}