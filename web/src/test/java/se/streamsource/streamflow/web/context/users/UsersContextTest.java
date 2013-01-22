/**
 *
 * Copyright 2009-2012 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.context.users;

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.administration.NewUserDTO;
import se.streamsource.streamflow.web.context.ContextTest;
import se.streamsource.streamflow.web.context.administration.OrganizationUsersContext;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.user.Users;

/**
 * JAVADOC
 */
public class UsersContextTest
   extends ContextTest
{
   // Helper methods
   public static void createUser(String name) throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      RoleMap.newCurrentRoleMap();
      playRole( Users.class, UsersEntity.USERS_ID);

      Organizations.Data data = (Organizations.Data) uow.get( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID );
      OrganizationEntity organization = (OrganizationEntity) data.organization().get();
      playRole( Organization.class, organization.identity().get() );
      context( OrganizationUsersContext.class).createuser( value( NewUserDTO.class, "{'username':'"+name+"','password':'"+name+"'}") );
      uow.complete();
   }

   @Test
   public void testCreateUser() throws UnitOfWorkCompletionException
   {
      // Create user
      clearEvents();
      createUser( "testing" );
      eventsOccurred( "createdUser", "joinedOrganization" );

      // Check that user can be found
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( Users.class, UsersEntity.USERS_ID);

         Assert.assertThat( valueContains( context(OrganizationUsersContext.class).index(), "testing" ), equalTo(true ));

         uow.discard();
      }
   }
}
