/**
 *
 * Copyright 2009-2013 Jayway Products AB
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

import static org.qi4j.api.util.Iterables.count;

import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.context.ContextTest;
import se.streamsource.streamflow.web.context.administration.CaseTypesContext;
import se.streamsource.streamflow.web.context.administration.OrganizationalUnitsContext;
import se.streamsource.streamflow.web.context.administration.OrganizationsContext;
import se.streamsource.streamflow.web.context.administration.ProjectsContext;
import se.streamsource.streamflow.web.context.administration.SelectedCaseTypesContext;
import se.streamsource.streamflow.web.context.administration.labels.LabelsContext;
import se.streamsource.streamflow.web.context.administration.labels.SelectedLabelsContext;
import se.streamsource.streamflow.web.context.administration.surface.emailaccesspoints.EmailAccessPointAdministrationContext;
import se.streamsource.streamflow.web.context.administration.surface.emailaccesspoints.EmailAccessPointsAdministrationContext;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;

/**
 * JAVADOC
 */
public class EmailAccessPointsContextTest
   extends ContextTest
{
   // Helper methods
   public static void createEmailAccessPoint(String email) throws UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      RoleMap.newCurrentRoleMap();

      playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
      playRole( Organization.class, findLink(context(OrganizationsContext.class).index(), "Organization"));

      context( EmailAccessPointsAdministrationContext.class).create("streamsourceflow@gmail.com");

      uow.complete();
   }

   public static void removeEmailAccessPoint( String email ) throws IOException, UnitOfWorkCompletionException
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      RoleMap.newCurrentRoleMap();

      playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
      playRole( OrganizationalUnits.class, findLink(context(OrganizationsContext.class).index(), "Organization"));

      playRole(findDescribable(context(EmailAccessPointsAdministrationContext.class).index(), email));

      context( EmailAccessPointAdministrationContext.class).delete();

      uow.complete();
   }

   @BeforeClass
   public static void before() throws UnitOfWorkCompletionException
   {
      OrganizationalUnitsContextTest.createOU( "OU1" );
      ProjectsContextTest.createProject("OU1", "Project1");
      clearEvents();
   }

   @AfterClass
   public static void after() throws IOException, UnitOfWorkCompletionException
   {
      ProjectsContextTest.removeProject("OU1", "Project1");
      OrganizationalUnitsContextTest.removeOU( "OU1" );
      clearEvents();
   }

   @Test
   public void testEmailAccessPoints() throws UnitOfWorkCompletionException, IOException
   {
      // Create case type and label
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

         playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
         playRole( OrganizationalUnits.class, findLink(context(OrganizationsContext.class).index(), "Organization"));
         playRole( findDescribable(context(OrganizationalUnitsContext.class).index(), "OU1"));
         playRole( findDescribable(context(ProjectsContext.class).index(), "Project1"));

         context(CaseTypesContext.class).create("Some casetype");

         context(SelectedCaseTypesContext.class).addcasetype(entityValue(findLink(context(SelectedCaseTypesContext.class).possiblecasetypes(), "Some casetype")));

         playRole(findDescribable(context(CaseTypesContext.class).index(), "Some casetype"));

         context(LabelsContext.class).create("Label 1");

         context(SelectedLabelsContext.class).addlabel(ContextTest.<Label>entity(findLink(context(SelectedLabelsContext.class).possiblelabels(), "Label 1")));

         uow.complete();
         clearEvents();

         createEmailAccessPoint("streamsourceflow@gmail.com");
      }

      // Set casetype and label
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();

         playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
         playRole( OrganizationalUnits.class, findLink( context( OrganizationsContext.class).index(), "Organization" ));
         playRole( findDescribable(context(OrganizationalUnitsContext.class).index(), "OU1"));
         playRole( context(EmailAccessPointsAdministrationContext.class).index().iterator().next());

         Assert.assertThat( count(context(ProjectsContext.class).index()), CoreMatchers.equalTo( 1L ));
         uow.discard();
      }
   }
}