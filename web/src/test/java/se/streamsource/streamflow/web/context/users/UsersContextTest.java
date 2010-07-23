/**
 *
 * Copyright 2009-2010 Streamsource AB
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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import se.streamsource.streamflow.resource.user.NewUserCommand;
import se.streamsource.streamflow.resource.user.UserEntityDTO;
import se.streamsource.streamflow.web.context.ContextTest;
import se.streamsource.streamflow.web.context.organizations.OrganizationContext;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * JAVADOC
 */
public class UsersContextTest
   extends ContextTest
{
   // Helper methods
   public static void createUser(String name) throws UnitOfWorkCompletionException
   {
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         root().users().createuser( value( NewUserCommand.class, "{'username':'"+name+"','password':'"+name+"'}") );
         uow.complete();
      }

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         OrganizationContext org = subContext(root().organizations(), "Organization");
         org.users().join( entityValue("test" ));
         uow.complete();
      }
   }

   public static void removeUser(String name) throws IOException, UnitOfWorkCompletionException
   {
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         OrganizationContext org = subContext(root().organizations(), "Organization");
         org.users().context( name ).delete();
         uow.complete();
      }
   }

   @Test
   public void testCreateUser() throws UnitOfWorkCompletionException
   {
      // Create user
      clearEvents();
      createUser( "test" );
      eventsOccurred( "createdUser", "joinedOrganization" );

      // Check that user can be found
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

         Assert.assertThat( valueContains( root().users().users(), "test" ), equalTo(true ));

         uow.discard();
      }
   }
}
