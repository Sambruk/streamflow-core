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

package se.streamsource.streamflow.web.context.organizations;

import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import se.streamsource.streamflow.resource.user.NewUserCommand;
import se.streamsource.streamflow.web.context.ContextTest;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * JAVADOC
 */
public class GroupsContextTest
   extends ContextTest
{
   // Helper methods
   public static void createGroup(String ouName, String name) throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      OrganizationContext org = subContext(root().organizations(),"Organization");
      OrganizationalUnitContext ou = subContext(org.organizationalunits(), ouName);
      GroupsContext groups = ou.groups();

      groups.creategroup( stringValue(name) );
      uow.complete();
   }

   public static void removeGroup( String ouName, String name ) throws IOException, UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      OrganizationContext org = subContext(root().organizations(),"Organization");
      OrganizationalUnitContext ou = subContext(org.organizationalunits(), ouName);
      GroupsContext groups = ou.groups();

      GroupContext group = subContext(groups, name);
      group.delete();
      uow.complete();
   }

   @BeforeClass
   public static void before() throws UnitOfWorkCompletionException
   {
      OrganizationalUnitsContextTest.createOU( "OU1" );
      clearEvents();
   }

   @AfterClass
   public static void after() throws IOException, UnitOfWorkCompletionException
   {
      OrganizationalUnitsContextTest.removeOU( "OU1" );
      clearEvents();
   }

   @Test
   public void testGroups() throws UnitOfWorkCompletionException, IOException
   {
      // Create group
      {
         createGroup( "OU1", "Group1" );
         eventsOccurred( "createdGroup", "changedDescription", "addedGroup", "changedOwner" );
      }

      // Check that group can be found
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         OrganizationContext org = subContext(root().organizations(),"Organization");
         OrganizationalUnitContext ou = subContext(org.organizationalunits(), "OU1");
         GroupsContext groups = ou.groups();

         Assert.assertThat( groups.index().links().get().size(), CoreMatchers.equalTo( 1 ));
         uow.discard();
      }

      // Remove group
      {
         removeGroup("OU1", "Group1");
         eventsOccurred( "removedGroup", "changedRemoved" );
      }
   }
}