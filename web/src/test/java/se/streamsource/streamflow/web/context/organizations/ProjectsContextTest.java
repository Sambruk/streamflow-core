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
import se.streamsource.streamflow.web.context.ContextTest;

import java.io.IOException;

/**
 * JAVADOC
 */
public class ProjectsContextTest
   extends ContextTest
{
   // Helper methods
   public static void createProject(String ouName, String name) throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      OrganizationContext org = subContext(root().organizations(),"Organization");
      OrganizationalUnitContext ou = subContext(org.organizationalunits(), ouName);
      ProjectsContext projects = ou.projects();

      projects.createproject( stringValue(name) );
      uow.complete();
   }

   public static void removeProject( String ouName, String name ) throws IOException, UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      OrganizationContext org = subContext(root().organizations(),"Organization");
      OrganizationalUnitContext ou = subContext(org.organizationalunits(), ouName);
      ProjectsContext projects = ou.projects();

      ProjectContext project = subContext(projects, name);
      project.delete();
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
   public void testProjects() throws UnitOfWorkCompletionException, IOException
   {
      // Create project
      {
         createProject( "OU1", "Project1" );
         eventsOccurred( "createdProject", "addedProject", "changedOwner", "changedDescription" );
      }

      // Check that project can be found
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         OrganizationContext org = subContext(root().organizations(),"Organization");
         OrganizationalUnitContext ou = subContext(org.organizationalunits(), "OU1");
         ProjectsContext projects = ou.projects();

         Assert.assertThat( projects.index().links().get().size(), CoreMatchers.equalTo( 1 ));
         uow.discard();
      }

      // Remove group
      {
         removeProject("OU1", "Project1");
         eventsOccurred( "removedProject", "changedRemoved" );
      }
   }
}