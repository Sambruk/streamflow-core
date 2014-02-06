/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.util.Iterables;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.application.security.UserPrincipal;
import se.streamsource.streamflow.web.context.ContextTest;
import se.streamsource.streamflow.web.context.administration.OrganizationalUnitContext;
import se.streamsource.streamflow.web.context.administration.OrganizationalUnitsContext;
import se.streamsource.streamflow.web.context.administration.OrganizationsContext;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;

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
      RoleMap.setCurrentRoleMap( new RoleMap() );

      RoleMap.current().set( new UserPrincipal("administrator") );
      playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
      playRole( OrganizationalUnits.class, findLink( context( OrganizationsContext.class).index(), "Organization" ));
      context( OrganizationalUnitsContext.class).create( name );
      uow.complete();
   }

   public static void removeOU( String name ) throws IOException, UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      RoleMap.setCurrentRoleMap( new RoleMap() );

      playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
      playRole( OrganizationalUnits.class, findLink( context( OrganizationsContext.class).index(), "Organization" ));
      playRole( findDescribable(context( OrganizationalUnitsContext.class ).index(), name));
      context( OrganizationalUnitContext.class).delete();
      uow.complete();
   }

   @Test
   public void testOrganizationalUnit() throws UnitOfWorkCompletionException, IOException
   {
      // Create OU
      {
         clearEvents();
         createOU("OU1");
         eventsOccurred( "createdOrganizationalUnit", "addedOrganizationalUnit", "changedOwner", "changedDescription", "grantedRole" );
      }

      // Check that OU can be found
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.setCurrentRoleMap( new RoleMap() );

         playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
         playRole( OrganizationalUnits.class, findLink( context( OrganizationsContext.class).index(), "Organization" ));
         OrganizationalUnitsContext org = context( OrganizationalUnitsContext.class);

         Assert.assertThat( Iterables.count(org.index()), CoreMatchers.equalTo( 1L ));
         uow.discard();
      }

      // Remove OU
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.setCurrentRoleMap( new RoleMap() );

         playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
         playRole( OrganizationalUnits.class, findLink( context( OrganizationsContext.class).index(), "Organization" ));
         playRole(findDescribable(context(OrganizationalUnitsContext.class).index(), "OU1"));
         context( OrganizationalUnitContext.class).delete();
         uow.complete();
         eventsOccurred( "removedOrganizationalUnit", "changedRemoved" );
      }
   }
}