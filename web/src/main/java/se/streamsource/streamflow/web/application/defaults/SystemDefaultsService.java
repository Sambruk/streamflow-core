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
package se.streamsource.streamflow.web.application.defaults;

import static org.qi4j.api.usecase.UsecaseBuilder.newUsecase;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.Drafts;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationalUnitsQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.created.Creator;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;
import se.streamsource.streamflow.web.domain.structure.user.Users;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

/**
 * A service holding system default configuration properties.
 */
@Mixins(SystemDefaultsService.Mixin.class)
public interface SystemDefaultsService
      extends ServiceComposite, Configuration, Activatable
{

   public Configuration<SystemDefaultsConfiguration> config();

   public void createCaseOnEmailFailure( EmailValue email );
   
   public Drafts getUser( EmailValue email );

   abstract class Mixin
         implements SystemDefaultsService, Activatable
   {
      @Structure
      Module module;
      
      @Service
      CachingService cache;

      Caching caching;

      @This
      Configuration<SystemDefaultsConfiguration> config;
      
      public Configuration<SystemDefaultsConfiguration> config()
      {
         return config;
      }

      public void activate() throws Exception
      {
         // Read arbitrary property just to activate config-handler
         config().configuration().enabled();
         caching = new Caching(cache, Caches.CASECOUNTS);

      }

      public void createCaseOnEmailFailure( EmailValue email )
      {

         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( newUsecase( "Create case on email failure" ) );
         RoleMap.newCurrentRoleMap();

         try
         {

            Organizations.Data organizations = uow.get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID );
            Organization organization = organizations.organization().get();
            OrganizationalUnit ou = ((OrganizationalUnitsQueries) organization).getOrganizationalUnitByName( config.configuration().supportOrganizationName().get() );
            Project project = ou.getProjectByName( config.configuration().supportProjectName().get() );
            CaseType caseType = project.getCaseTypeByName( config.configuration().supportCaseTypeForIncomingEmailName().get() );

            Drafts user = getUser( email );
            ConversationParticipant participant = (ConversationParticipant) user;


            RoleMap.current().set( organization );
            RoleMap.current().set( project );
            RoleMap.current().set( user );

            CaseEntity caze = user.createDraft();
            caze.changeCaseType( caseType );
            caze.changeOwner( project );

            RoleMap.current().set( caze );

            caze.caselog().get().addTypedEntry( "{receivererror,description=Could not parse email.}", CaseLogEntryTypes.system );

            caze.changeDescription( email.subject().get() );
            caze.addNote( email.content().get() );

            // Create conversation
            Conversation conversation = caze.createConversation( email.subject().get(), (Creator) user );
            conversation.createMessage( email.content().get(), participant );

            // Create attachments
            for (AttachedFileValue attachedFileValue : email.attachments().get())
            {
               Attachment attachment = caze.createAttachment( attachedFileValue.uri().get() );
               attachment.changeName( attachedFileValue.name().get() );
               attachment.changeMimeType( attachedFileValue.mimeType().get() );
               attachment.changeModificationDate( attachedFileValue.modificationDate().get() );
               attachment.changeSize( attachedFileValue.size().get() );
               attachment.changeUri( attachedFileValue.uri().get() );
            }

            // Add contact info
            caze.updateContact(0, ((Contactable.Data)user).contact().get());

            // open the case
            caze.open();

            caching.addToCache( ((ProjectEntity)project).identity().get(), 1 );

            uow.complete();

         } catch (Exception e)
         {
            uow.discard();
         } finally
         {
            RoleMap.clearCurrentRoleMap();
         }
      }

      public Drafts getUser(EmailValue email)
      {
         // Try to find real user first
         Query<Drafts> finduserwithemail = module.queryBuilderFactory().newNamedQuery(Drafts.class, module.unitOfWorkFactory().currentUnitOfWork(), "finduserwithemail");
         finduserwithemail.setVariable("email", "[{\"contactType\":\"HOME\",\"emailAddress\":\"" + email.from().get() + "\"}]");
         Drafts user = finduserwithemail.find();

         // Create email user
         if (user == null)
         {
            user = module.unitOfWorkFactory().currentUnitOfWork().get(Users.class, UsersEntity.USERS_ID).createEmailUser(email);
         }

         return user;
      }
   }
}
