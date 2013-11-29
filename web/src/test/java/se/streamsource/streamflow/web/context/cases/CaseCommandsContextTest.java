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
package se.streamsource.streamflow.web.context.cases;

import org.apache.commons.collections.ArrayStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.api.Contexts;
import se.streamsource.dci.api.InteractionConstraints;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.streamflow.web.application.security.UserPrincipal;
import se.streamsource.streamflow.web.context.ContextTest;
import se.streamsource.streamflow.web.context.administration.CaseTypesContext;
import se.streamsource.streamflow.web.context.administration.MembersContext;
import se.streamsource.streamflow.web.context.administration.OrganizationalUnitsContext;
import se.streamsource.streamflow.web.context.administration.OrganizationsContext;
import se.streamsource.streamflow.web.context.administration.ProjectsContext;
import se.streamsource.streamflow.web.context.administration.ResolutionsContext;
import se.streamsource.streamflow.web.context.administration.SelectedCaseTypesContext;
import se.streamsource.streamflow.web.context.administration.SelectedResolutionsContext;
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
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.gtd.IdGenerator;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.*;
import static org.junit.Assert.*;
import static org.qi4j.api.util.Iterables.*;

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
         ProjectsContextTest.createProject( "OU1", "Project2" );
      }

      Project project1;
      Organization org;
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "1" ));
         RoleMap.newCurrentRoleMap();
         playRole( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID );
         org = playRole( Organization.class, findLink( context( OrganizationsContext.class ).index(), "Organization" ) );
         playRole( findDescribable( context( OrganizationalUnitsContext.class ).index(), "OU1" ) );

         project1 = findDescribable( context( ProjectsContext.class ).index(), "Project1" );
         playRole( project1 );

         CaseTypesContext caseTypes = context( CaseTypesContext.class );
         caseTypes.create( "CaseType1" );

         SelectedCaseTypesContext selectedCaseTypes = context( SelectedCaseTypesContext.class );
         selectedCaseTypes.addcasetype( entityValue( findLink( selectedCaseTypes.possiblecasetypes(), "CaseType1" ) ) );

         playRole( findDescribable( caseTypes.index(), "CaseType1" ) );
         ResolutionsContext resolutionsContext = context( ResolutionsContext.class );
         resolutionsContext.create( "Resolution1" );
         SelectedResolutionsContext selectedResolutionsContext = context( SelectedResolutionsContext.class );
         selectedResolutionsContext.addresolution( entityValue( findLink( selectedResolutionsContext.possibleresolutions(), "Resolution1" ) ) );

         uow.complete();
      }

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "2" ));
         RoleMap.newCurrentRoleMap();
         playRole( org );
         playRole( project1 );
         context( LabelsContext.class ).create( "Label1" );
         SelectedLabelsContext context = context( SelectedLabelsContext.class );
         context.addlabel( CaseCommandsContextTest.<Label>entity( findLink( context.possiblelabels(), "Label1" ) ) );
         uow.complete();
      }

      UsersContextTest.createUser( "testing" );

      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "3" ));
         RoleMap.newCurrentRoleMap();
         playRole( project1 );
         context( MembersContext.class ).addmember( entityValue( "testing" ) );
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
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "4" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );
         DraftsContext drafts = context( DraftsContext.class );
         drafts.createcase();
         uow.complete();
         eventsOccurred( "createdCase", "addedContact" );
      }

      // Check that draft exists
      Case caze;
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "5" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );

         DraftsContext drafts = context( DraftsContext.class );
         Iterable<Case> caseList = drafts.cases( valueBuilderFactory.newValueFromJSON( TableQuery.class, "{tq:'select *'}" ) );
         assertThat( Iterables.count( caseList ), equalTo( 1L ) );
         caze = first( caseList );
         uow.discard();
      }

      // Name draft
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "6" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );
         playRole( caze );

         context( CaseGeneralCommandsContext.class ).changedescription( "Case1" );
         uow.complete();
         eventsOccurred( "changedDescription" );
      }

      // Check actions for new draft
      {
         checkActions( caze, "delete", "sendto" );
      }

      // Send to project
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "7" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );
         playRole( caze );

         CaseCommandsContext context = context( CaseCommandsContext.class );
         context.sendto( entityValue( findLink( context.possiblesendto(), "Project1" ) ) );
         uow.complete();
         eventsOccurred( "changedOwner", "changedDate", "setCounter", "assignedCaseId" );
      }

      // Check actions for draft sent to project
      {
         checkActions( caze, "delete", "open", "sendto" );
      }

      // Select casetype
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "8" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );
         playRole( caze );

         CaseGeneralCommandsContext context = context( CaseGeneralCommandsContext.class );
         context.casetype( entityValue( findLink( context.possiblecasetypes(), "CaseType1" ) ) );

         uow.complete();
         eventsOccurred( "changedCaseType" );
      }

      // Add label
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "9" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );
         playRole( caze );

         LabelableContext context = context( LabelableContext.class );
         context.addlabel( entityValue( findLink( context.possiblelabels(), "Label1" ) ) );

         uow.complete();
         eventsOccurred( "addedLabel" );
      }

      // Open case
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "10" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );
         playRole( caze );

         context( CaseCommandsContext.class ).open();

         uow.complete();
         eventsOccurred( "changedStatus" );
      }

      // Check open actions
      {
         checkActions( caze, "delete", "resolve", "read", "markread", "sendto", "restrict", "assign", "assignto" );
      }

      // Assign case
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "11" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );

         playRole( Project.class, findLink( context( WorkspaceProjectsContext.class ).index(), "Project1" ) );
// TODO This is random!         playRole( first( context( InboxContext.class ).index() ) );
         playRole( caze );

         context( CaseCommandsContext.class ).assign();

         uow.complete();
         eventsOccurred( "assignedTo" );
      }

      // Check assigned actions
      {
         checkActions( caze, "delete", "resolve", "markread", "read", "sendto", "restrict", "unassign" );
      }

      // Resolve case
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "12" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );

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
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "13" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );
         playRole( caze );

         context( CaseCommandsContext.class ).reopen();

         uow.complete();
         eventsOccurred( "changedStatus", "unresolved" );
      }

      // Check reopened actions
      {
         checkActions( caze, "delete", "resolve", "markread", "read", "sendto", "restrict", "unassign" );
      }

      // Close
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "14" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );
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

   @Test
   public void testConcurrentSendToProject() throws UnitOfWorkCompletionException
   {
      long currentId = 0;
      // Create draft1
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "21" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );
         DraftsContext drafts = context( DraftsContext.class );
         drafts.createcase();

         Organizations.Data organizations = uow.get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID );
         currentId = ((IdGenerator.Data)((OrganizationEntity)organizations.organization().get())).current().get();

         uow.complete();
         eventsOccurred( "createdCase", "addedContact" );
      }

      // Check that draft exists
      Case caze1;
      UnitOfWork uowCaze1 = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "22" ));
      {
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );

         DraftsContext drafts = context( DraftsContext.class );
         Iterable<Case> caseList = drafts.cases( valueBuilderFactory.newValueFromJSON( TableQuery.class, "{tq:'select *'}" ) );
         //assertThat( Iterables.count( caseList ), equalTo( 1L ) );
         caze1 = first( caseList );
      }

      // Create draft2
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "23" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );
         DraftsContext drafts = context( DraftsContext.class );
         drafts.createcase();
         uow.complete();
         eventsOccurred( "createdCase", "addedContact" );
      }

      // Check that draft exists
      Case caze2;
      UnitOfWork uowCaze2 = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "24" ));
      {
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );

         DraftsContext drafts = context( DraftsContext.class );
         Iterable<Case> caseList = drafts.cases( valueBuilderFactory.newValueFromJSON( TableQuery.class, "{tq:'select *'}" ) );
         //assertThat( Iterables.count( caseList ), equalTo( 2L ) );
         caze2 = first( caseList );
      }

      // Send to project
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "25" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );
         playRole( caze1 );

         CaseCommandsContext context = context( CaseCommandsContext.class );
         context.sendto( entityValue( findLink( context.possiblesendto(), "Project1" ) ) );
         uow.complete();
         eventsOccurred( "changedOwner", "changedDate", "setCounter", "assignedCaseId" );
      }


      // Send to project2
      {
         UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "26" ));
         RoleMap.newCurrentRoleMap();
         playRole( User.class, "testing" );
         RoleMap.current().set( new UserPrincipal( "testing" ) );
         playRole( caze2 );

         CaseCommandsContext context = context( CaseCommandsContext.class );
         context.sendto( entityValue( findLink( context.possiblesendto(), "Project2" ) ) );
         uow.complete();
         eventsOccurred( "changedOwner", "changedDate", "setCounter", "assignedCaseId" );
      }
      String caseUUID1 = EntityReference.getEntityReference( caze1 ).identity();
      String caseUUID2 = EntityReference.getEntityReference( caze2 ).identity();
      uowCaze1.discard();
      uowCaze2.discard();

      SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd" );
      String date = sdf.format( new Date() );

      UnitOfWork readUow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "27" ));
      RoleMap.newCurrentRoleMap();
      playRole( User.class, "testing" );
      RoleMap.current().set( new UserPrincipal( "testing" ) );
      DraftsContext drafts = context( DraftsContext.class );
      Iterable<Case> caseList = drafts.cases( valueBuilderFactory.newValueFromJSON( TableQuery.class, "{tq:'select *'}" ) );
      long caseCount = currentId + Iterables.count( caseList );

      assertTrue( "expected " + date +"-" + (caseCount - 1) ,  ((CaseId.Data) readUow.get( Case.class, caseUUID1 )).caseId().get().equals( date + "-" + (caseCount - 1) ) );
      assertTrue( "expected " + date +"-" + (caseCount), ((CaseId.Data) readUow.get( Case.class, caseUUID2 )).caseId().get().equals( date + "-" + (caseCount) ) );

      readUow.discard();
   }

   private void checkActions( Case caze, String... allowedActions )
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "28" ));
      RoleMap.newCurrentRoleMap();
      RoleMap.current().set( new UserPrincipal( "testing" ) );
      playRole( caze );
      playRole( User.class, "testing" );
      RoleMap.current().set( new UserPrincipal( "testing" ) );

      List<String> actions = new ArrayStack();
      Iterables.addAll( actions, Iterables.map( new Function<Method, String>()
      {
         public String map( Method method )
         {
            return method.getName();
         }
      }, Contexts.commands( CaseCommandsContext.class, constraints, RoleMap.current(), moduleInstance ) ) );

      assertThat( actions, containsInAnyOrder( allowedActions ));
      uow.discard();
   }
}
