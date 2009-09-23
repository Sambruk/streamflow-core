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
import org.qi4j.api.query.Query;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.web.domain.organization.Organization;
import se.streamsource.streamflow.web.domain.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.role.Role;
import se.streamsource.streamflow.web.domain.user.UserEntity;

import java.util.logging.Logger;

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
            try{
                OrganizationsEntity organizations;
                try
                {
                    // Check if organizations entity exists
                    organizations = uow.get(OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID);
                } catch (NoSuchEntityException e)
                {
                    // Create bootstrap data
                    organizations = uow.newEntity(OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID);
                }

                // Check if admin exists
                UserEntity admin;
                try
                {
                    admin = uow.get(UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME);
                } catch (NoSuchEntityException e)
                {
                    // Create admin
                    admin = organizations.createUser(UserEntity.ADMINISTRATOR_USERNAME, UserEntity.ADMINISTRATOR_USERNAME);

                    ValueBuilder<ContactValue> contactBuilder = vbf.newValueBuilder(ContactValue.class);
                    contactBuilder.prototype().name().set("Administrator");
                    ContactValue contact = contactBuilder.newInstance();
                    admin.updateContact(contact);
                }


                Query<OrganizationEntity> orgs = organizations.findAll();

                if (orgs.count() == 0)
                {
                    // Create default organization
                    Organization ou = organizations.createOrganization("Organization");
                    uow.apply();
                }

                for (OrganizationEntity org : orgs)
                {
                    Role administrator;
                    if (org.roles().count() == 0)
                    {
                        // Administrator role
                        administrator = org.createRole("Administrator");
                    } else
                    {
                        administrator = org.roles().get(0);
                    }

                    // Administrator should be member of all organizations
                    if (!admin.organizations().contains(org))
                    {
                        admin.join(org);
                    }
    
                    // Assign admin role to administrator
                    org.grantRole(admin, administrator);
                }

                uow.complete();

            } catch (Exception e)
            {
                Logger.getLogger(this.getClass().getName()).warning("BootstrapDataService faild to start!");
                e.printStackTrace();
                uow.discard();
            }
        }

        public void passivate() throws Exception
        {
        }
    }
}