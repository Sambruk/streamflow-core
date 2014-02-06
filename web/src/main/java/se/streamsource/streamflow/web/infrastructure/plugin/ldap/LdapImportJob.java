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
package se.streamsource.streamflow.web.infrastructure.plugin.ldap;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactPhoneDTO;
import se.streamsource.streamflow.server.plugin.authentication.UserDetailsValue;
import se.streamsource.streamflow.server.plugin.ldapimport.GroupDetailsValue;
import se.streamsource.streamflow.server.plugin.ldapimport.GroupListValue;
import se.streamsource.streamflow.server.plugin.ldapimport.GroupMemberDetailValue;
import se.streamsource.streamflow.server.plugin.ldapimport.UserListValue;
import se.streamsource.streamflow.web.domain.entity.ExternalReference;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersEntity;
import se.streamsource.streamflow.web.domain.interaction.security.Authentication;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.group.Participants;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.infrastructure.plugin.LdapImporterServiceConfiguration;

/**
 * A quartz job responsible for import of users an groups from ldap.
 */
@Mixins(LdapImportJob.Mixin.class)
public interface LdapImportJob extends Job, TransientComposite
{
   void importUsers() throws UnitOfWorkCompletionException;
   void importGroups();

   abstract class Mixin
         implements LdapImportJob
   {
      Logger logger = LoggerFactory.getLogger( LdapImportJob.class );

      @Structure
      Module module;

      @Optional @Uses
      Configuration<LdapImporterServiceConfiguration> config;

      private static Usecase addUserUsecase = UsecaseBuilder.newUsecase( "Import new user" );
      private static Usecase updateUserUsecase = UsecaseBuilder.newUsecase("Update user from import");

      private static Usecase addGroupUsecase = UsecaseBuilder.newUsecase( "Import new group" );
      private static Usecase updateGroupUsecase = UsecaseBuilder.newUsecase("Update group from import");


      /**
       * Import users from LDAP. Check local users against users fetched from LDAP.
       * If a local user is not present in LDAP disable him. If a user fetched from LDAP does not exist locally create it with a dummy password.
       * If a ldap user exists locally but has changed properties update the local user with changed properties.
       * @throws UnitOfWorkCompletionException
       */
      public void importUsers()
      {
         String json = callPlugin( "users" );

         final UserListValue externalUserListValue = module.valueBuilderFactory().newValueFromJSON(UserListValue.class, json);

         handleUsersToLeaveOrganization( externalUserListValue.users().get() );

         handleCreateAndUpdateOfLocalUsers( externalUserListValue.users().get() );
      }

      private void handleCreateAndUpdateOfLocalUsers( final List<UserDetailsValue> externalUsers )
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase("Handle create and update local users") );

         // discover if we need to insert new user or have to do an update.
         for( UserDetailsValue userDetail : externalUsers )
         {
            Authentication localUser = null;
            try
            {
               localUser = uow.get( Authentication.class, userDetail.username().get() );
            } catch (NoSuchEntityException ne )
            {
               // ok - do nothing
            }

            if (localUser == null)
            {
               createNewUser(userDetail, userDetail.username().get() );
            } else
            {
               updateUser(userDetail, (Contactable.Data) localUser );
            }
         }
         // changes where handled downstream so we can discard this uow to avoid
         // ConcurrentEntityModificationException
         uow.discard();
      }

      private void handleUsersToLeaveOrganization( final List<UserDetailsValue> externalUsers )
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase("Handle users to leave organization") );

         UsersEntity usersEntity = uow.get( UsersEntity.class, UsersEntity.USERS_ID );
         Organization org = ((Organizations.Data)uow.get( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID )).organization().get();



         // compare local users with ldap users and deactivate if they are not present in ldap.
         Iterable<UserEntity> usersNotInLdap = Iterables.filter( new Specification<UserEntity>()
         {
            public boolean satisfiedBy( final UserEntity user )
            {
               return !Iterables.matchesAny( new Specification<UserDetailsValue>()
               {
                  public boolean satisfiedBy( UserDetailsValue userDetail )
                  {
                     // not interested in administrator or already unjoined or users existing in ldap
                     return user.isAdministrator() || user.organizations().count() == 0 ||
                           user.userName().get().equals( userDetail.username().get() );
                  }
               }, externalUsers );
            }
         }, usersEntity.users() );

         for( UserEntity user : usersNotInLdap )
         {
            user.leave( org );
         }
         try
         {
            uow.complete();
         } catch (UnitOfWorkCompletionException e)
         {
            logger.error( "Could not commit handle users to leave organization.", e );
            uow.discard();
         }
      }

      public void importGroups()
      {
         String json = callPlugin( "groups" );

         final GroupListValue externalGroupListValue = module.valueBuilderFactory().newValueFromJSON( GroupListValue.class, json );

         handleRemoveGroups( externalGroupListValue.groups().get() );

         handleCreateOrUpdateLocalGroups( externalGroupListValue.groups().get() );

         // cycle once more for update of participants
         // a new uow will be created downstream for these changes
         for( GroupDetailsValue externalGroup : externalGroupListValue.groups().get() )
         {
            synchronizeParticipants( externalGroup );
         }
      }

      private void handleCreateOrUpdateLocalGroups( final List<GroupDetailsValue> externalGroups )
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase("Handle create and update local groups") );

         Groups localGroups = (Groups)uow.get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID ).organization().get();

         // create or update groups without touching participants just yet
         for( GroupDetailsValue externalGroup : externalGroups )
         {
            Group localGroup = null;

            localGroup = ((Groups)localGroups).findByExternalReference( externalGroup.id().get() );

            if( localGroup == null)
            {
               createNewGroupOnOrganization( externalGroup );
            } else
            {
               updateGroupOnOrganization( localGroup, externalGroup.name().get() );
            }
         }

         uow.discard();
      }

      private void updateGroupOnOrganization( Group localGroup, String name )
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase("Handle update local group") );
         Group group = uow.get( localGroup );

         group.changeDescription( name );

         try
         {
            uow.complete();
         } catch (UnitOfWorkCompletionException e)
         {
            logger.error( "Could not update local group", e );
            uow.discard();
         }

      }

      private void handleRemoveGroups( final List<GroupDetailsValue> externalGroups )
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase("Handle remove groups") );

         Groups localGroups = (Groups)uow.get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID ).organization().get();

         // first compare local groups and ldap groups and delete local groups that are no longer available in LDAP.
         Iterable<Group> groupsNotInLdap = Iterables.filter( new Specification<Group>()
         {
            public boolean satisfiedBy( final Group group )
            {
               return !Iterables.matchesAny( new Specification<GroupDetailsValue>()
               {
                  public boolean satisfiedBy( GroupDetailsValue groupDetail )
                  {
                     return groupDetail.id().get().equals( ((ExternalReference.Data) group).reference().get() );
                  }
               }, externalGroups );
            }
         }, ((Groups.Data) localGroups).groups() );

         // fetch list from iterables iterator to avoid ConcurrentModificationException during remove operation.
         List<Group> toRemove = (List<Group>)IteratorUtils.toList( groupsNotInLdap.iterator() );

         for( Group group : toRemove )
         {
            localGroups.removeGroup( group );
         }

         try
         {
            uow.complete();
         } catch (UnitOfWorkCompletionException e)
         {
            logger.error( "Could not remove groups.", e );
            uow.discard();
         }
      }

      private void synchronizeParticipants(final GroupDetailsValue externalGroup )
      {
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( updateGroupUsecase );

         final Groups groups = (Groups)uow.get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID ).organization().get();

         final Group group = groups.findByExternalReference( externalGroup.id().get() );

         if( group != null )
         {
            // check first if there are any participants in local group that are not present in LDAP and let them leave the group
            Iterable<Participant> participantsNotPresentInLdap = Iterables.filter( new Specification<Participant>()
            {
               public boolean satisfiedBy( final Participant participant )
               {
                  return !Iterables.matchesAny( new Specification<GroupMemberDetailValue>()
                  {
                     public boolean satisfiedBy( GroupMemberDetailValue externalParticipant )
                     {
                        return EntityReference.getEntityReference( participant ).identity().equals( extractInternalIdForParticipant( externalParticipant, groups ) );
                     }
                  }, externalGroup.members().get() );
               }
            }, ((Participants.Data)group).participants() );


            for ( Participant participant : participantsNotPresentInLdap )
            {
               group.removeParticipant( participant );
            }

            // check the other way round and join group
            Iterable<GroupMemberDetailValue> ldapParticipantNotPresentLocally = Iterables.filter( new Specification<GroupMemberDetailValue>()
            {
               public boolean satisfiedBy( final GroupMemberDetailValue externalParticipant )
               {
                  return !Iterables.matchesAny( new Specification<Participant>()
                  {
                     public boolean satisfiedBy( Participant localParticipant )
                     {
                        return EntityReference.getEntityReference( localParticipant ).identity().equals( extractInternalIdForParticipant( externalParticipant, groups ) );
                     }
                  }, ((Participants.Data) group).participants() );
               }
            }, externalGroup.members().get() );

            for( GroupMemberDetailValue externalMember : ldapParticipantNotPresentLocally )
            {
               Participant participant = uow.get( Participant.class, extractInternalIdForParticipant( externalMember, groups ) );
               group.addParticipant( participant );
            }

            try
            {
               uow.complete();
            } catch (UnitOfWorkCompletionException uowe )
            {
               logger.error( "Could not commit member synchronization.", uowe );
               uow.discard();
            }
         } else
            throw new IllegalArgumentException( "The group we try to synchronize participants for does not exist!" );

      }

      private String extractInternalIdForParticipant( GroupMemberDetailValue externalParticipant, Groups orgGroups )
      {
         String externalId = "";
         switch ( externalParticipant.memberType().get() )
         {
            case user:
               externalId = externalParticipant.id().get();
               break;
            case group:
               try
               {
                  externalId = EntityReference.getEntityReference( orgGroups.findByExternalReference( externalParticipant.id().get() ) ).identity();
               } catch (NullPointerException npe )
               {
                  logger.error( "Not able to find any imported group with DN: " + externalParticipant.id().get() );
                  throw new IllegalArgumentException( "Not able to find any imported group with DN: " + externalParticipant.id().get(), npe );
               }
               break;
         }
         return externalId;
      }

      private void createNewGroupOnOrganization( GroupDetailsValue externalGroup )
      {

         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( addGroupUsecase );

         Organization org = uow.get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID ).organization().get();

         Group group = org.createGroup( externalGroup.name().get() );
         group.changeReference( externalGroup.id().get() );

         try
         {
            uow.complete();
         } catch (UnitOfWorkCompletionException e)
         {
            logger.error( "Import of new group failed.", e );
            uow.discard();
         }
      }

      private void updateUser(UserDetailsValue externalUser, Contactable.Data user )
      {
         boolean modified = false;

         if( ((OrganizationParticipations.Data)user).organizations().count() == 0 )
         {
            modified = true;
         }

         if (!externalUser.name().get().equals(user.contact().get().name().get()))
         {
            modified = true;
         }

         List contactEmailList = user.contact().get().emailAddresses().get();
         if (!externalUser.emailAddress().get()
               .equals( contactEmailList.size() == 0 ? ""
                     : ((ContactEmailDTO) contactEmailList.get( 0 )).emailAddress().get() ))
         {
            modified = true;
         }

         List contactPhoneList = user.contact().get().phoneNumbers().get();
         if (!externalUser.phoneNumber().get()
               .equals( contactPhoneList.size() == 0 ? ""
                     : ((ContactPhoneDTO)contactPhoneList.get( 0 )).phoneNumber().get() ))
         {
            modified = true;
         }

         if (modified)
         {
            UnitOfWork unitOfWork = module.unitOfWorkFactory().newUnitOfWork( updateUserUsecase );
            UserEntity userEntity = unitOfWork.get( (UserEntity) user );
            Organization org = unitOfWork.get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID ).organization().get();

            userEntity.changeDescription( externalUser.name().get() );
            userEntity.join( org );

            ValueBuilder<ContactDTO> contactBuilder = module.valueBuilderFactory().newValueBuilder(ContactDTO.class);
            contactBuilder.prototype().name().set(externalUser.name().get());
            ValueBuilder<ContactEmailDTO> emailBuilder = module.valueBuilderFactory().newValueBuilder(ContactEmailDTO.class);
            emailBuilder.prototype().emailAddress().set(externalUser.emailAddress().get());
            contactBuilder.prototype().emailAddresses().get().add(emailBuilder.newInstance());
            ValueBuilder<ContactPhoneDTO> phoneBuilder = module.valueBuilderFactory().newValueBuilder(ContactPhoneDTO.class);
            phoneBuilder.prototype().phoneNumber().set(externalUser.phoneNumber().get());
            contactBuilder.prototype().phoneNumbers().get().add(phoneBuilder.newInstance());
            ((Contactable) userEntity).updateContact(contactBuilder.newInstance());

            try
            {
               unitOfWork.complete();
            } catch (UnitOfWorkCompletionException e)
            {
               logger.error( "Update of imported user failed.", e );
               unitOfWork.discard();
            }
         }
      }

      private void createNewUser(UserDetailsValue externalUser, String username )
      {

         UnitOfWork unitOfWork = module.unitOfWorkFactory().newUnitOfWork( addUserUsecase );
         UsersEntity usersEntity = unitOfWork.get( UsersEntity.class, UsersEntity.USERS_ID );

         User user = usersEntity.createUser( username, "ett2tre!magicString" );

         ValueBuilder<ContactDTO> contactBuilder = module.valueBuilderFactory().newValueBuilder(ContactDTO.class);
         contactBuilder.prototype().name().set(externalUser.name().get());
         ValueBuilder<ContactEmailDTO> emailBuilder = module.valueBuilderFactory().newValueBuilder(ContactEmailDTO.class);
         emailBuilder.prototype().emailAddress().set(externalUser.emailAddress().get());
         contactBuilder.prototype().emailAddresses().get().add(emailBuilder.newInstance());
         ValueBuilder<ContactPhoneDTO> phoneBuilder = module.valueBuilderFactory().newValueBuilder(ContactPhoneDTO.class);
         phoneBuilder.prototype().phoneNumber().set(externalUser.phoneNumber().get());
         contactBuilder.prototype().phoneNumbers().get().add(phoneBuilder.newInstance());
         ((Contactable) user).updateContact(contactBuilder.newInstance());

         Organization org = unitOfWork.get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID ).organization().get();
         user.join( org );

         try
         {
            unitOfWork.complete();
         } catch (UnitOfWorkCompletionException e)
         {
            logger.error( "Create of new imported user failed.", e );
            unitOfWork.discard();
         }
      }

      private String callPlugin( String command )
      {
         String json = "";

         ClientResource clientResource = new ClientResource(config.configuration().url().get() + "/import/" + command );

         try
         {
            // Call plugin
            Representation result = clientResource.get();


            try
            {
               json = result.getText();
            } catch (IOException e)
            {
               throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED,
                     "Could not get userdetails for externally validated user");
            }


         } catch (ResourceException e)
         {
            //TODO do what?
         }
         return json;
      }

      public void execute( JobExecutionContext context ) throws JobExecutionException
      {
         try
         {
            config = (Configuration<LdapImporterServiceConfiguration>)context.getJobDetail().getJobDataMap().get( "config" );
            logger.info( "Start LDAP import" );
            importUsers();
            importGroups();
            logger.info("Finished LDAP import");
         } catch (Throwable e)
         {
            logger.error("Could not complete import from LDAP", e);
         }
      }
   }
}
