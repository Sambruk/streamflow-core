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

package se.streamsource.streamflow.web.context.caze;

import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.application.security.UserPrincipal;
import se.streamsource.streamflow.web.context.ContextTest;
import se.streamsource.streamflow.web.context.organizations.CaseTypeContext;
import se.streamsource.streamflow.web.context.organizations.OrganizationContext;
import se.streamsource.streamflow.web.context.organizations.OrganizationalUnitContext;
import se.streamsource.streamflow.web.context.organizations.OrganizationalUnitsContextTest;
import se.streamsource.streamflow.web.context.organizations.ProjectContext;
import se.streamsource.streamflow.web.context.organizations.ProjectsContextTest;
import se.streamsource.streamflow.web.context.users.UserContextTest;
import se.streamsource.streamflow.web.context.users.UsersContextTest;
import se.streamsource.streamflow.web.context.users.workspace.DraftsContext;
import se.streamsource.streamflow.web.context.users.workspace.WorkspaceProjectContext;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Check lifecycle of a case
 */
public class CaseActionsContextTest
   extends ContextTest
{
   @BeforeClass
   public static void before() throws UnitOfWorkCompletionException
   {
      {
         OrganizationalUnitsContextTest.createOU( "OU1" );
      }

      {
         ProjectsContextTest.createProject( "OU1", "Project1" );
      }

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         OrganizationContext org = subContext(root().organizations(),"Organization");
         OrganizationalUnitContext ou = subContext(org.organizationalunits(), "OU1");
         ProjectContext project = subContext(ou.projects(), "Project1");
         project.casetypes().createcasetype( stringValue("CaseType1") );
         CaseTypeContext caseTypeContext = subContext(project.casetypes(), "CaseType1");
         caseTypeContext.resolutions().createresolution( stringValue("Resolution1") );
         caseTypeContext.selectedresolutions().addresolution( entityValue(findLink(caseTypeContext.resolutions().index(), "Resolution1").id().get()) );
         uow.complete();
      }

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         OrganizationContext org = subContext(root().organizations(),"Organization");
         OrganizationalUnitContext ou = subContext(org.organizationalunits(), "OU1");
         ProjectContext project = subContext(ou.projects(), "Project1");
         project.selectedcasetypes().addcasetype( entityValue(findLink(project.casetypes().index(), "CaseType1").id().get() ));
         uow.complete();
      }

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         OrganizationContext org = subContext(root().organizations(),"Organization");
         OrganizationalUnitContext ou = subContext(org.organizationalunits(), "OU1");
         ProjectContext project = subContext(ou.projects(), "Project1");
         project.labels().createlabel( stringValue("Label1") );
         uow.complete();
      }

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         OrganizationContext org = subContext(root().organizations(),"Organization");
         OrganizationalUnitContext ou = subContext(org.organizationalunits(), "OU1");
         ProjectContext project = subContext(ou.projects(), "Project1");
         project.selectedlabels().addlabel( entityValue(findLink(project.labels().index(), "Label1").id().get() ));
         uow.complete();
      }

      UsersContextTest.createUser("test");

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         OrganizationContext org = subContext(root().organizations(),"Organization");
         OrganizationalUnitContext ou = subContext(org.organizationalunits(), "OU1");
         ProjectContext project = subContext(ou.projects(), "Project1");
         project.members().addmember( entityValue("test") );
         uow.complete();
         clearEvents();
      }
   }

   @Test
   public void testCaseActions() throws UnitOfWorkCompletionException
   {
      // Create draft
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         DraftsContext drafts = root().users().context("test").workspace().user().drafts();
         drafts.createdraft();
         uow.complete();
         eventsOccurred("createdCase", "addedContact" );
      }

      // Check that draft exists
      String caseId;
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         DraftsContext drafts = root(reference("http://localhost/streamflow/")).users().context("test").workspace().user().drafts();
         List<LinkValue> caseList = drafts.cases().links().get();
         assertThat( caseList.size(), equalTo( 1 ));
         caseId = caseList.get( 0 ).id().get();
         uow.discard();
      }

      // Name draft
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         CaseContext caze = root().cases().context( caseId );
         caze.general().changedescription( stringValue("Case1") );
         uow.complete();
         eventsOccurred("changedDescription" );
      }

      // Check actions for new draft
      {
         checkActions( caseId, "sendto", "delete" );
      }

      // Send to project
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         CaseContext caze = root().cases().context( caseId );
         caze.sendto( entityValue(findLink(caze.possiblesendto(), "Project1").id().get()) );
         uow.complete();
         eventsOccurred("changedOwner", "changedDate", "setCounter", "assignedCaseId" );
      }

      // Check actions for draft sent to project
      {
         checkActions( caseId, "open", "sendto", "delete" );
      }

      // Select casetype
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         CaseContext caze = root().cases().context( caseId );
         caze.general().casetype( entityValue(findLink(caze.general().possiblecasetypes(), "CaseType1").id().get()) );
         uow.complete();
         eventsOccurred("changedCaseType" );
      }

      // Add label
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         CaseContext caze = root().cases().context( caseId );
         caze.general().addlabel( entityValue(findLink(caze.general().possiblelabels(), "Label1").id().get()) );
         uow.complete();
         eventsOccurred("addedLabel" );
      }

      // Open case
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         CaseContext caze = root().cases().context( caseId );
         caze.open();
         uow.complete();
         eventsOccurred("changedStatus" );
      }

      // Check open actions
      {
         checkActions( caseId, "assign", "sendto", "close", "delete" );
      }

      // Assign case
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         WorkspaceProjectContext project = subContext(root(user("test"), reference("http://localhost/streamflow/")).users().context("test").workspace().projects(), "Project1");
         root(user("test")).cases().context( findLink(project.inbox().cases(), "Case1").id().get()).assign();
         uow.complete();
         eventsOccurred("assignedTo");
      }

      // Check assigned actions
      {
         checkActions( caseId, "sendto", "unassign", "onhold", "close", "delete" );
      }

      // Resolve case
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         uow.metaInfo().set( new UserPrincipal("test") );
         CaseContext caze = root(user("test")).cases().context( caseId );
         caze.resolve( entityValue(findLink(caze.possibleresolutions(), "Resolution1").id().get()) );
         uow.complete();
         eventsOccurred("resolved", "changedStatus" );
      }

      // Check resolved actions
      {
         checkActions( caseId, "reopen" );
      }

      // Reopen case
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         uow.metaInfo().set( new UserPrincipal("test") );
         CaseContext caze = root(user("test")).cases().context( caseId );
         caze.reopen();
         uow.complete();
         eventsOccurred("changedStatus", "unresolved" );
      }

      // Check reopened actions
      {
         checkActions( caseId, "sendto", "unassign", "onhold", "close", "delete" );
      }
      
      // Close
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         uow.metaInfo().set( new UserPrincipal("test") );
         CaseContext caze = root(user("test")).cases().context( caseId );
         caze.close();
         uow.complete();
         eventsOccurred("changedStatus" );
      }

      // Check closed actions
      {
         checkActions( caseId, "reopen" );
      }
   }

   private void checkActions( String caseId, String... allowedActions )
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      CaseContext caze = root(user("test")).cases().context( caseId );
      List<String> actions = caze.actions().actions().get();
      assertThat(actions, equalTo( asList( allowedActions )));
      uow.discard();
   }
}
