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
import org.qi4j.api.structure.Application;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.api.usecase.UsecaseBuilder.*;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.web.domain.group.Group;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.Role;
import se.streamsource.streamflow.web.domain.user.UserEntity;

/**
 * JAVADOC
 */
@Mixins(TestDataService.TestDataMixin.class)
public interface TestDataService
        extends ServiceComposite, Activatable
{
    class TestDataMixin
            implements Activatable
    {
        @Structure
        Application app;

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        public void activate() throws Exception
        {
            UnitOfWork uow = uowf.newUnitOfWork(newUsecase("Test data"));

            UserEntity user = uow.get(UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME);

            OrganizationalUnitEntity ou = (OrganizationalUnitEntity) user.organizations().iterator().next();
            ou.describe("WayGroup");

            // Create suborganizations
            OrganizationalUnit jayway = ou.createOrganizationalUnit("Jayway");
            ou.createOrganizationalUnit("Dotway");
            ou.createOrganizationalUnit("Realway");

            // Create groups
            Group cc = ou.createGroup("Contact center");
            ou.createGroup("Park management");

            cc.addParticipant(user);

            Role agent = ou.createRole("Agent");
            Role manager = ou.createRole("Manager");

            // Create tasks
            for (int i = 0; i < 30; i++)
                user.createTask().describe("Arbetsuppgift " + i);

            // Create project
            Project project = ou.createProject("Information query");

            // Create labels
            project.createLabel().describe("Question");
            project.createLabel().describe("Issue chase");
            project.createLabel().describe("Suggestion");

            project.createMember(user);
            project.addRole(user, agent);

            // Create project
            Project parks = ou.createProject("City parks");

            parks.createMember(cc);
            parks.addRole(cc, agent);
            parks.createMember(user);
            parks.addRole(user, manager);

            // Create tasks
            for (int i = 0; i < 30; i++)
                project.createTask().describe("Arbetsuppgift " + i);

            // Create labels
            user.createLabel().describe("Label 1");
            user.createLabel().describe("Label 2");
            user.createLabel().describe("Label 3");

            uow.complete();
        }

        public void passivate() throws Exception
        {
        }
    }
}