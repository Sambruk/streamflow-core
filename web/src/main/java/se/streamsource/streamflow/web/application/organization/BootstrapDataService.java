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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.domain.contact.Contactable;
import se.streamsource.streamflow.web.domain.organization.Organization;
import se.streamsource.streamflow.web.domain.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.user.User;
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
                // Check if organizations entity exists
                uow.get(OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID);
                uow.discard();
            } catch (NoSuchEntityException e)
            {
                // Create bootstrap data
                OrganizationsEntity organizations = uow.newEntity(OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID);

                Organization ou = organizations.createOrganization("Organization");

                // User
                User user = organizations.createUser(UserEntity.ADMINISTRATOR_USERNAME, UserEntity.ADMINISTRATOR_USERNAME);

                ValueBuilder<ContactValue> contactBuilder = vbf.newValueBuilder(ContactValue.class);
                contactBuilder.prototype().name().set("Administrator");
                ContactValue contact = contactBuilder.newInstance();
                ((Contactable) user).updateContact(contact);

                // Join organization
                ((OrganizationParticipations) user).join(ou);

                uow.complete();
            }

        }

        public void passivate() throws Exception
        {
        }
    }
}