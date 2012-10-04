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
package se.streamsource.streamflow.web.application.statistics;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.factory.DomainEventFactoryService;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.TransactionTrackerConfiguration;
import se.streamsource.streamflow.infrastructure.time.TimeService;
import se.streamsource.streamflow.web.application.security.UserPrincipal;
import se.streamsource.streamflow.web.context.administration.GroupsContext;
import se.streamsource.streamflow.web.domain.entity.attachment.AttachmentEntity;
import se.streamsource.streamflow.web.domain.entity.caselog.CaseLogEntity;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.entity.casetype.ResolutionEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.entity.conversation.MessageEntity;
import se.streamsource.streamflow.web.domain.entity.label.LabelEntity;
import se.streamsource.streamflow.web.domain.entity.note.NotesTimeLineEntity;
import se.streamsource.streamflow.web.domain.entity.organization.GroupEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationalUnitEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.organization.PriorityEntity;
import se.streamsource.streamflow.web.domain.entity.organization.RoleEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.note.NoteValue;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.organization.ParticipantRolesValue;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.Users;
import se.streamsource.streamflow.web.infrastructure.event.MemoryEventStoreService;

import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * JAVADOC
 */
public class CaseStatisticsServiceTest
        extends AbstractQi4jTest
{
   public void assemble(ModuleAssembly module) throws AssemblyException
   {
      new RdfMemoryStoreAssembler().assemble(module);

      module.services(MemoryEntityStoreService.class);

      module.services( CaseStatisticsService.class,
            MemoryEventStoreService.class,
            UuidIdentityGeneratorService.class,
            LoggingStatisticsStore.class ).instantiateOnStartup();
      module.importedServices( TimeService.class ).importedBy( ImportedServiceDeclaration.NEW_OBJECT );
      module.services( DomainEventFactoryService.class ).instantiateOnStartup();


      module.entities(StatisticsConfiguration.class);
      module.forMixin(TransactionTrackerConfiguration.class).declareDefaults().enabled().set(true);

      module.entities(LabelEntity.class,
              OrganizationsEntity.class,
              OrganizationEntity.class,
              OrganizationalUnitEntity.class,
              ProjectEntity.class,
              GroupEntity.class,
              UsersEntity.class,
              RoleEntity.class,
              UserEntity.class,
              NotesTimeLineEntity.class,
              ResolutionEntity.class,
              CaseEntity.class,
              CaseTypeEntity.class,
              ConversationEntity.class,
              MessageEntity.class,
              AttachmentEntity.class,
              CaseLogEntity.class,
              PriorityEntity.class );
      
      module.values(ContactDTO.class, ParticipantRolesValue.class, CaseLogEntryDTO.class, NoteValue.class, CaseLogEntryValue.class, PriorityValue.class );

      module.objects(TimeService.class, CaseStatisticsServiceTest.class);
      module.transients(GroupsContext.class);

      module.values(DomainEvent.class, TransactionDomainEvents.class);
      module.values(CaseStatisticsValue.class, FormFieldStatisticsValue.class, RelatedStatisticsValue.class, OrganizationalStructureValue.class, OrganizationalUnitValue.class);
   }

   @Test
   public void testLabelChangeDescription() throws UnitOfWorkCompletionException, InterruptedException
   {
      TestAppender appender = new TestAppender();
      Logger.getRootLogger().addAppender(appender);

      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      LabelEntity label1 = uow.newEntity(LabelEntity.class, "label1");
      label1.changeDescription("Foo bar");
      uow.complete();

      Thread.sleep(1000);

      assertThat(appender.getEvents().get(0).getMessage().toString(), equalTo("id:label1, description:Foo bar, type:label"));
   }

   @Test
   public void testCaseClosedAndRemoved() throws UnitOfWorkCompletionException, PrivilegedActionException, InterruptedException
   {
      TestAppender appender = new TestAppender();
      Logger.getRootLogger().addAppender(appender);

      final UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();

      Users users = uow.newEntity(UsersEntity.class, UsersEntity.USERS_ID);
      final UserEntity user1 = (UserEntity) users.createUser("user1", "userpwd");

      RoleMap.newCurrentRoleMap();
      RoleMap.current().set(new UserPrincipal(user1.userName().get()));
      RoleMap.current().set(user1);

      final String[] id = new String[1];

      Organizations orgs = uow.newEntity(Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID);
      Organization org = orgs.createOrganization("Organization1");
      OrganizationalUnit ou1 = org.createOrganizationalUnit("OU1");
      Group group1 = moduleInstance.transientBuilderFactory().newTransientBuilder(GroupsContext.class).use(ou1).newInstance().create("Group1");
      group1.addParticipant(user1);
      Project project1 = ou1.createProject("Project1");
      project1.addMember(group1);

      CaseTypeEntity caseType1 = (CaseTypeEntity) project1.createCaseType("Casetype");
      caseType1.changeDescription("Casetype");
      Resolution fixed = caseType1.createResolution("Fixed");

      Label label1 = caseType1.createLabel("Label1");

      uow.complete();


      UnitOfWork caseUoW = unitOfWorkFactory.newUnitOfWork();
      UserEntity user = caseUoW.get(user1);
      caseType1 = caseUoW.get(caseType1);
      project1 = caseUoW.get(project1);
      fixed = caseUoW.get(fixed);
      label1 = caseUoW.get(label1);

      //RoleMap.current().set( user1, null );
      RoleMap.current().set( user );

      CaseEntity case1 = user.createDraft();
      case1.changeDescription("Case description");
      case1.addNote( "Case note" );
      case1.addLabel(label1);
      case1.changeCaseType(caseType1);
      case1.changeOwner(project1);
      case1.open();
      case1.assignTo(user);
      case1.resolve(fixed);
      case1.close();

      id[0] = case1.identity().get();

      caseUoW.complete();

      Thread.sleep(1000);

      // Verify log records
      int idx = 0;
      assertThat(appender.getEvents().get(idx).getMessage().toString(), new ContainsMatcher("description:Organization1, type:organization"));
      assertThat(appender.getEvents().get(++idx).getMessage().toString(), new ContainsMatcher("New organizational structure:"));
      assertThat(appender.getEvents().get(++idx).getMessage().toString(), new ContainsMatcher("description:OU1, type:organizationalUnit"));
      assertThat(appender.getEvents().get(++idx).getMessage().toString(), new ContainsMatcher("description:Group1, type:group"));
      assertThat(appender.getEvents().get(++idx).getMessage().toString(), new ContainsMatcher("description:Project1"));
      assertThat(appender.getEvents().get(++idx).getMessage().toString(), new ContainsMatcher("description:Casetype"));
      assertThat(appender.getEvents().get(++idx).getMessage().toString(), new ContainsMatcher("description:Fixed"));
      assertThat(appender.getEvents().get(++idx).getMessage().toString(), new ContainsMatcher("description:Label1"));
      assertThat(appender.getEvents().get(++idx).getMessage().toString(), new ContainsMatcher("description:Case description"));

      UnitOfWork removeUoW = unitOfWorkFactory.newUnitOfWork();
      case1 = removeUoW.get(CaseEntity.class, id[0]);
      case1.deleteEntity();
      removeUoW.complete();

      Thread.sleep(1000);

      assertThat(appender.getEvents().get(++idx).getMessage().toString(), new ContainsMatcher("Removed statistics about"));
   }

   public static class TestAppender
           extends AppenderSkeleton
   {
      List<LoggingEvent> events = new ArrayList<LoggingEvent>();

      @Override
      protected void append(LoggingEvent event)
      {
         if (event.getLevel().isGreaterOrEqual(Level.INFO))
            events.add(event);
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

      public RegularMatcher(String regex)
      {
         this.regex = regex;
      }

      public boolean matches(Object o)
      {
         return Pattern.matches(regex, o.toString());
      }

      public void describeTo(Description description)
      {
         description.appendText("Blah");
      }
   }

   public static class ContainsMatcher
           extends BaseMatcher
   {
      String str;

      public ContainsMatcher(String str)
      {
         this.str = str;
      }

      public boolean matches(Object o)
      {
         return o.toString().contains(str);
      }

      public void describeTo(Description description)
      {
         description.appendText(str);
      }
   }
}
