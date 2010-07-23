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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import se.streamsource.streamflow.web.context.ContextTest;

import java.io.IOException;

/**
 * JAVADOC
 */
public class OrganizationalUnitsContextTest
   extends ContextTest
{
   // Helper methods
   public static void createOU(String name) throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      OrganizationContext org = subContext(root().organizations(),"Organization");
      org.organizationalunits().createorganizationalunit( stringValue( name) );
      uow.complete();
   }

   public static void removeOU( String name ) throws IOException, UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      OrganizationContext org = subContext(root().organizations(),"Organization");
      OrganizationalUnitContext ou = subContext(org.organizationalunits(), name);

      ou.delete();
      uow.complete();
   }

   @Test
   public void testOrganizationalUnit() throws UnitOfWorkCompletionException, IOException
   {
      // Create OU
      {
         clearEvents();
         createOU("OU1");
         eventsOccurred( "createdOrganizationalUnit", "addedOrganizationalUnit", "changedOwner", "changedDescription" );
      }

      // Check that OU can be found
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         OrganizationContext org = subContext(root().organizations(),"Organization");

         Assert.assertThat( org.organizationalunits().index().links().get().size(), CoreMatchers.equalTo( 1 ));
         uow.discard();
      }

      // Remove OU
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         OrganizationContext org = subContext(root().organizations(),"Organization");
         OrganizationalUnitContext ou = subContext(org.organizationalunits(), "OU1");

         ou.delete();
         uow.complete();
         eventsOccurred( "removedOrganizationalUnit", "changedRemoved" );
      }
   }
}