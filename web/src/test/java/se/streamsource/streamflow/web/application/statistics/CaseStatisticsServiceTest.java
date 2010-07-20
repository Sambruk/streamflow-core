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

package se.streamsource.streamflow.web.application.statistics;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.RdfIndexingEngineService;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.DomainEventFactoryService;
import se.streamsource.streamflow.infrastructure.event.TimeService;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.helper.TransactionTrackerConfiguration;
import se.streamsource.streamflow.infrastructure.event.source.memory.MemoryEventStoreService;
import se.streamsource.streamflow.web.application.security.UserPrincipal;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.casetype.ResolutionEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.organization.RoleEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.organization.ParticipantRolesValue;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.Users;
import se.streamsource.streamflow.web.infrastructure.event.EventSourceService;

import javax.security.auth.Subject;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * JAVADOC
 */
public class CaseStatisticsServiceTest
      extends AbstractQi4jTest
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      new RdfMemoryStoreAssembler().assemble( module );

      module.addServices( MemoryEntityStoreService.class );

      module.addServices( CaseStatisticsService.class ).instantiateOnStartup();
      module.addServices( MemoryEventStoreService.class, EventSourceService.class );
      module.addServices( UuidIdentityGeneratorService.class);
      module.importServices( TimeService.class ).importedBy( ImportedServiceDeclaration.NEW_OBJECT );
      module.addServices( LoggingStatisticsStore.class );
      module.addServices( DomainEventFactoryService.class );

      module.addEntities( StatisticsConfiguration.class );
      module.forMixin( TransactionTrackerConfiguration.class ).declareDefaults().enabled().set( true );

      module.addEntities( LabelEntity.class,
            OrganizationsEntity.class,
            OrganizationEntity.class,
            OrganizationalUnitEntity.class,
            ProjectEntity.class,
            GroupEntity.class,
            UsersEntity.class,
            RoleEntity.class,
            UserEntity.class,
            ResolutionEntity.class,
            CaseEntity.class,
            CaseTypeEntity.class );
      module.addValues( ContactValue.class, ParticipantRolesValue.class);

      module.addObjects( TimeService.class, CaseStatisticsServiceTest.class );

      module.addValues( DomainEvent.class, TransactionEvents.class );
      module.addValues( CaseStatisticsValue.class, RelatedStatisticsValue.class );
   }

   @Test
   public void testLabelChangeDescription() throws UnitOfWorkCompletionException
   {
      TestAppender appender = new TestAppender();
      Logger.getRootLogger().addAppender( appender );

      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      LabelEntity label1 = uow.newEntity( LabelEntity.class, "label1" );
      label1.changeDescription( "Foo bar" );
      uow.complete();

      assertThat( appender.getEvents().get( 0 ).getMessage().toString(), equalTo( "id:label1, description:Foo bar, type:label" ));
   }

   @Test
   public void testCaseClosed() throws UnitOfWorkCompletionException, PrivilegedActionException
   {
      TestAppender appender = new TestAppender();
      Logger.getRootLogger().addAppender( appender );

      final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

      Users users = uow.newEntity( UsersEntity.class, UsersEntity.USERS_ID );
      final UserEntity user1 = (UserEntity) users.createUser( "user1", "user1" );

      Subject subject = new Subject();
      subject.getPrincipals().add( new UserPrincipal(user1.userName().get()) );

      Subject.doAs( subject, new PrivilegedExceptionAction()
      {
         public Object run() throws Exception
         {
            Organizations orgs = uow.newEntity( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID );
            Organization org = orgs.createOrganization( "Organization1" );
            OrganizationalUnit ou1 = org.createOrganizationalUnit( "OU1" );
            Group group1 = ou1.createGroup( "Group1" );
            group1.addParticipant( user1 );
            Project project1 = ou1.createProject( "Project1" );
            project1.addMember( group1 );

            CaseTypeEntity caseType1 = (CaseTypeEntity) project1.createCaseType( "Casetype" );
            caseType1.changeDescription( "Casetype" );
            Resolution fixed = caseType1.createResolution( "Fixed" );

            Label label1 = caseType1.createLabel( "Label1" );

            uow.complete();

            UnitOfWork caseUoW = unitOfWorkFactory.newUnitOfWork();
            UserEntity user = caseUoW.get( user1 );
            caseType1 = caseUoW.get( caseType1 );
            project1 = caseUoW.get( project1 );
            fixed = caseUoW.get( fixed );
            label1 = caseUoW.get( label1 );

            CaseEntity case1 = user.createDraft();
            case1.changeDescription( "Case description" );
            case1.changeNote( "Case note" );
            case1.addLabel( label1 );
            case1.changeCaseType( caseType1 );
            case1.changeOwner( (Owner) project1 );
            case1.open();
            case1.assignTo( user );
            case1.resolve( fixed );
            case1.close();

            caseUoW.complete();

            return null;
         }
      });


      // Verify log records
      int idx = 0;
      assertThat( appender.getEvents().get( idx ).getMessage().toString(),new ContainsMatcher("description:Organization1, type:organization" ));
      assertThat( appender.getEvents().get( ++idx ).getMessage().toString(),new ContainsMatcher("description:OU1, type:organizationalUnit" ));
      assertThat( appender.getEvents().get( ++idx ).getMessage().toString(),new ContainsMatcher("description:Group1, type:group" ));
      assertThat( appender.getEvents().get( ++idx ).getMessage().toString(),new ContainsMatcher("description:Project1" ));
      assertThat( appender.getEvents().get( ++idx ).getMessage().toString(),new ContainsMatcher("description:Label1" ));
      assertThat( appender.getEvents().get( ++idx ).getMessage().toString(),new ContainsMatcher("description:Case description" ));
      assertThat( appender.getEvents().get( idx ).getMessage().toString(),new ContainsMatcher("note:Case note" ));
   }

   public static class TestAppender
      extends AppenderSkeleton
   {
      List<LoggingEvent> events = new ArrayList<LoggingEvent>( );

      @Override
      protected void append( LoggingEvent event )
      {
         events.add( event );
      }

      public boolean requiresLayout()
      {
         return false;
      }

      public void close()
      {
      }

      public List<LoggingEvent> getEvents()
      {
         return events;
      }
   }

   public static class RegularMatcher
      extends BaseMatcher
   {
      String regex;

      public RegularMatcher( String regex )
      {
         this.regex = regex;
      }

      public boolean matches( Object o )
      {
         return Pattern.matches( regex, o.toString() );
      }

      public void describeTo( Description description )
      {
         description.appendText( "Blah" );
      }
   }

   public static class ContainsMatcher
      extends BaseMatcher
   {
      String str;

      public ContainsMatcher( String str )
      {
         this.str = str;
      }

      public boolean matches( Object o )
      {
         return o.toString().contains( str );
      }

      public void describeTo( Description description )
      {
         description.appendText( str );
      }
   }
}
