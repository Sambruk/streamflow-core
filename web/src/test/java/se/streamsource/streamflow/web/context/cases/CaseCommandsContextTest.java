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

package se.streamsource.streamflow.web.context.cases;

import org.apache.commons.collections.ArrayStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.api.Contexts;
import se.streamsource.dci.api.InteractionConstraints;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.web.application.security.UserPrincipal;
import se.streamsource.streamflow.web.context.ContextTest;
import se.streamsource.streamflow.web.context.administration.*;
import se.streamsource.streamflow.web.context.administration.labels.LabelsContext;
import se.streamsource.streamflow.web.context.administration.labels.SelectedLabelsContext;
import se.streamsource.streamflow.web.context.organizations.OrganizationalUnitsContextTest;
import se.streamsource.streamflow.web.context.organizations.ProjectsContextTest;
import se.streamsource.streamflow.web.context.users.UsersContextTest;
import se.streamsource.streamflow.web.context.workspace.DraftsContext;
import se.streamsource.streamflow.web.context.workspace.WorkspaceProjectsContext;
import se.streamsource.streamflow.web.context.workspace.cases.CaseCommandsContext;
import se.streamsource.streamflow.web.context.workspace.cases.general.CaseGeneralCommandsContext;
import se.streamsource.streamflow.web.context.workspace.cases.general.LabelableContext;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qi4j.api.util.Iterables.first;

/**
 * Check lifecycle of a case
 */
public class CaseCommandsContextTest
      extends ContextTest
{
   InteractionConstraints constraints;

   @BeforeClass
   public static void before() throws UnitOfWorkCompletionException
   {
      {
         OrganizationalUnitsContextTest.createOU( "OU1" );
      }

      {
         ProjectsContextTest.createProject( "OU1", "Project1" );
      }

      Project project1;
      Organization org;
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
         org = playRole( Organization.class, findLink( context( OrganizationsContext.class).index(), "Organization" ));
         playRole( findDescribable(context( OrganizationalUnitsContext.class).index(), "OU1"));

         project1 = findDescribable( context( ProjectsContext.class ).index(), "Project1" );
         playRole( project1 );

         CaseTypesContext caseTypes = context( CaseTypesContext.class );
         caseTypes.createcasetype( stringValue( "CaseType1" ) );

         SelectedCaseTypesContext selectedCaseTypes = context( SelectedCaseTypesContext.class );
         selectedCaseTypes.addcasetype( entityValue( findLink( selectedCaseTypes.possiblecasetypes(), "CaseType1" ) ) );

         playRole( CaseType.class, findLink(caseTypes.index(), "CaseType1") );
         ResolutionsContext resolutionsContext = context( ResolutionsContext.class );
         resolutionsContext.createresolution( stringValue( "Resolution1" ) );
         SelectedResolutionsContext selectedResolutionsContext = context( SelectedResolutionsContext.class );
         selectedResolutionsContext.addresolution( entityValue( findLink( selectedResolutionsContext.possibleresolutions(), "Resolution1" ) ) );

         uow.complete();
      }

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole(org);
         playRole( project1 );
         context( LabelsContext.class ).createlabel( stringValue( "Label1" ) );
         SelectedLabelsContext context = context( SelectedLabelsContext.class );
         context.addlabel( CaseCommandsContextTest.<Label>entity( findLink( context.possiblelabels(), "Label1" ) ) );
         uow.complete();
      }

      UsersContextTest.createUser( "test" );

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( project1 );
         context( MembersContext.class ).addmember( entityValue( "test" ) );
         uow.complete();
      }

      clearEvents();
   }

   @Before
   public void services()
   {
      constraints = serviceLocator.<InteractionConstraints>findService( InteractionConstraints.class ).get();
   }

   @Test
   public void testCaseActions() throws UnitOfWorkCompletionException
   {
      // Create draft
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "test" );
         RoleMap.current().set( new UserPrincipal("test") );
         DraftsContext drafts = context( DraftsContext.class );
         drafts.createcase();
         uow.complete();
         eventsOccurred( "createdCase", "addedContact" );
      }

      // Check that draft exists
      Case caze;
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "test" );
         RoleMap.current().set( new UserPrincipal("test") );

         DraftsContext drafts = context( DraftsContext.class );
         Iterable<Case> caseList = drafts.cases( valueBuilderFactory.newValueFromJSON( TableQuery.class, "{tq:'select *'}" ) );
         assertThat( Iterables.count( caseList ), equalTo( 1L ) );
         caze = first( caseList );
         uow.discard();
      }

      // Name draft
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "test" );
         RoleMap.current().set( new UserPrincipal("test") );
         playRole( caze );

         context( CaseGeneralCommandsContext.class ).changedescription( stringValue( "Case1" ) );
         uow.complete();
         eventsOccurred( "changedDescription" );
      }

      // Check actions for new draft
      {
         checkActions( caze, "delete","sendto", "createSubCase" );
      }

      // Send to project
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "test" );
         RoleMap.current().set( new UserPrincipal("test") );
         playRole( caze );

         CaseCommandsContext context = context( CaseCommandsContext.class );
         context.sendto( entityValue( findLink( context.possiblesendto(), "Project1" ) ) );
         uow.complete();
         eventsOccurred( "changedOwner", "changedDate", "setCounter", "assignedCaseId" );
      }

      // Check actions for draft sent to project
      {
         checkActions( caze, "delete", "open", "sendto", "createSubCase" );
      }

      // Select casetype
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "test" );
         RoleMap.current().set( new UserPrincipal("test") );
         playRole( caze );

         CaseGeneralCommandsContext context = context( CaseGeneralCommandsContext.class );
         context.casetype( entityValue( findLink( context.possiblecasetypes(), "CaseType1" ) ) );

         uow.complete();
         eventsOccurred( "changedCaseType" );
      }

      // Add label
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "test" );
         RoleMap.current().set( new UserPrincipal("test") );
         playRole( caze );

         LabelableContext context = context( LabelableContext.class );
         context.addlabel( entityValue( findLink( context.possiblelabels(), "Label1" ) ) );

         uow.complete();
         eventsOccurred( "addedLabel" );
      }

      // Open case
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "test" );
         RoleMap.current().set( new UserPrincipal("test") );
         playRole( caze );

         context( CaseCommandsContext.class ).open();

         uow.complete();
         eventsOccurred( "changedStatus" );
      }

      // Check open actions
      {
         checkActions( caze, "delete", "resolve", "sendto", "createSubCase", "assign" );
      }

      // Assign case
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "test" );
         RoleMap.current().set( new UserPrincipal("test") );

         playRole( Project.class, findLink( context( WorkspaceProjectsContext.class ).index(), "Project1" ) );
// TODO This is random!         playRole( first( context( InboxContext.class ).index() ) );
         playRole( caze );

         context( CaseCommandsContext.class ).assign();

         uow.complete();
         eventsOccurred( "assignedTo" );
      }

      // Check assigned actions
      {
         checkActions( caze, "delete", "resolve", "sendto", "createSubCase", "unassign", "onhold");
      }

      // Resolve case
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "test" );
         RoleMap.current().set( new UserPrincipal("test") );

         playRole( Project.class, findLink( context( WorkspaceProjectsContext.class ).index(), "Project1" ) );
//         playRole( first( context( AssignmentsContext.class ).index() ) );
         playRole( caze );

//         uow.metaInfo().set( new UserPrincipal("test") );

         CaseCommandsContext context = context( CaseCommandsContext.class );
         context.resolve( entityValue( findLink( context.possibleresolutions(), "Resolution1" ).id().get() ) );

         uow.complete();
         eventsOccurred( "resolved", "changedStatus" );
      }

      // Check resolved actions
      {
         checkActions( caze, "reopen" );
      }

      // Reopen case
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "test" );
         RoleMap.current().set( new UserPrincipal("test") );
         playRole( caze );

         context( CaseCommandsContext.class ).reopen();

         uow.complete();
         eventsOccurred( "changedStatus", "unresolved" );
      }

      // Check reopened actions
      {
         checkActions( caze, "delete", "resolve", "sendto", "createSubCase", "unassign", "onhold" );
      }

      // Close
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "test" );
         RoleMap.current().set( new UserPrincipal("test") );
         playRole( caze );

         context( CaseCommandsContext.class ).close();

         uow.complete();
         eventsOccurred( "changedStatus" );
      }

      // Check closed actions
      {
         checkActions( caze, "reopen" );
      }
   }

   private void checkActions( Case caze, String... allowedActions )
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      RoleMap.newCurrentRoleMap();
      RoleMap.current().set( new UserPrincipal( "test" ) );
      playRole( caze );
      playRole( User.class, "test" );
      RoleMap.current().set( new UserPrincipal("test") );

      List<String> actions = new ArrayStack(  );
      Iterables.addAll( actions, Iterables.map( new Function<Method, String>()
      {
         public String map( Method method )
         {
            return method.getName();
         }
      },Contexts.commands( CaseCommandsContext.class, constraints,  RoleMap.current(), moduleInstance) ));

      assertThat( actions, equalTo( asList( allowedActions ) ) );
      uow.discard();
   }
}
