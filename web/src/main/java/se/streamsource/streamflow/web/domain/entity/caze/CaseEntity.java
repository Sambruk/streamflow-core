/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.entity.caze;

import org.qi4j.api.Qi4j;
import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO;
import se.streamsource.streamflow.api.workspace.cases.conversation.MessageType;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Notable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.project.ProjectOrganizationalUnitQueries;
import se.streamsource.streamflow.web.domain.entity.user.EmailUserEntity;
import se.streamsource.streamflow.web.domain.entity.user.ProjectQueries;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.AssignIdSideEffect;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.interaction.gtd.Unread;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;
import se.streamsource.streamflow.web.domain.interaction.security.Authorization;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccess;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessDefaults;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessRestriction;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessType;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.attachment.FormAttachments;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLoggable;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.DefaultDaysToComplete;
import se.streamsource.streamflow.web.domain.structure.casetype.PriorityOnCase;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolvable;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.CasePriority;
import se.streamsource.streamflow.web.domain.structure.caze.Closed;
import se.streamsource.streamflow.web.domain.structure.caze.Contacts;
import se.streamsource.streamflow.web.domain.structure.caze.History;
import se.streamsource.streamflow.web.domain.structure.caze.Location;
import se.streamsource.streamflow.web.domain.structure.caze.Notes;
import se.streamsource.streamflow.web.domain.structure.caze.NotificationTrace;
import se.streamsource.streamflow.web.domain.structure.caze.Origin;
import se.streamsource.streamflow.web.domain.structure.caze.SubCase;
import se.streamsource.streamflow.web.domain.structure.caze.SubCases;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.created.Creator;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;
import se.streamsource.streamflow.web.domain.structure.form.SearchableForms;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.form.Submitter;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.task.DoubleSignatureTasks;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * This represents a single Case in the system
 */
@SideEffects(
{ AssignIdSideEffect.class, StatusClosedSideEffect.class, CaseEntity.CaseLogCaseEntitySideEffect.class,
      CaseEntity.UpdateSearchableFormsSideEffect.class, CaseEntity.EmailAccesspointSideEffect.class })
@Concerns(
{ CaseEntity.RemovableConcern.class, CaseEntity.TypedCaseAccessConcern.class,
      CaseEntity.TypedCaseDefaultDueOnConcern.class, CaseEntity.OwnableCaseAccessConcern.class,
      CaseEntity.CaseLogContactConcern.class, CaseEntity.CaseLogConversationConcern.class,
      CaseEntity.CaseLogAttachmentConcern.class, CaseEntity.CaseLogSubmittedFormsConcern.class,
      CaseEntity.AssignableConcern.class, CaseEntity.TypedCaseDefaultObligatoryPriorityConcern.class,
      CaseEntity.UnreadConcern.class, CaseEntity.OwnableUnreadConcern.class,
      CaseEntity.StatusConcern.class})
@Mixins(CaseEntity.AuthorizationMixin.class)
public interface CaseEntity
      extends Case,

      // Interactions
      Assignable.Events,
      Assignable.Data,
      Describable.Data,
      DueOn.Data,
      Notable.Data,
      Notes.Data,
      Ownable.Data,
      CaseId.Data,
      Status.Events,
      Status.Data,
      Conversations.Data,
      CaseAccess.Data,
      CaseAccessRestriction.Data,
      Unread.Data,

      // Structure
      Closed,
      Attachments.Data,
      FormAttachments.Data,
      Contacts.Data,
      Labelable.Data,
      Removable.Data,
      Resolvable.Data,
      FormDrafts.Data,
      SubmittedForms.Data,
      SearchableForms.Data,
      SearchableForms.Events,
      TypedCase.Data,
      SubCases.Data,
      SubCase.Data,
      History.Data,
      CaseLoggable.Data,
      CasePriority.Data,
      Origin,
      DoubleSignatureTasks.Data,
      NotificationTrace.Data,
      Location.Data,

      // Queries
      SubmittedFormsQueries,
      CaseTypeQueries,

      DomainEntity
{
   class AuthorizationMixin
         implements Authorization
   {
      @This
      CaseEntity aCase;

      @Structure
      Module module;

      public boolean hasPermission( String userId, String permission )
      {
         User actor = module.unitOfWorkFactory().currentUnitOfWork().get( User.class, userId );

         switch (aCase.status().get())
         {
            case DRAFT:
            {
               // Creator has all permissions
               return aCase.createdBy().get().equals( actor );
            }

            case OPEN:
            case CLOSED:
            case ON_HOLD:
            {
               CaseAccessType accessType = aCase.getAccessType( PermissionType.valueOf( permission ) );

               switch (accessType)
               {
                  case all:
                     return true;

                  case organization:
                  {
                     OwningOrganizationalUnit.Data owningOU = (OwningOrganizationalUnit.Data) aCase.owner().get();
                     OrganizationalUnit ou = owningOU.organizationalUnit().get();

                     return ou.isMemberOrParticipant( actor );
                  }

                  case project:
                  {
                     Project project = (Project) aCase.owner().get();

                     return actor.isMember( project );
                  }
                  
                  case sameoubranch:
                  {
                     // Find all top ou for the projects that the user is member in
                     ArrayList<OrganizationalUnit> topUnits = new ArrayList<OrganizationalUnit>();
                     for (Project project : ((ProjectQueries)actor).allProjects())
                     {
                        topUnits.add(((ProjectOrganizationalUnitQueries)((ProjectEntity)project)).topOU());
                     }
                     
                     // Find the top Ou for the owner to the case
                     OwningOrganizationalUnit.Data owningOU = (OwningOrganizationalUnit.Data) aCase.owner().get();
                     OrganizationalUnit ou = owningOU.organizationalUnit().get();
                     
                     Organizations.Data organizations = module.unitOfWorkFactory().currentUnitOfWork().get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID );
                     Organization organization = organizations.organization().get();
                     
                     while (!ou.isOwnedBy( organization )) {
                        ou = (OrganizationalUnit) ((Ownable.Data)ou).owner().get();
                     }
                     
                     return topUnits.contains( ou );
                        
                  }
               }
            }
         }

         return false; // Can never get here, but just in case
      }
   }

   class TypedCaseAccessConcern
         extends ConcernOf<TypedCase>
         implements TypedCase
   {
      @This
      CaseAccess caseAccess;

      public void changeCaseType( @Optional CaseType newCaseType )
      {
         next.changeCaseType( newCaseType );

         if (newCaseType != null)
         {
            // Transfer settings for security from new casetype to case
            for (Map.Entry<PermissionType, CaseAccessType> entry : ((CaseAccessDefaults.Data) newCaseType).accessPermissionDefaults().get().entrySet())
            {
               caseAccess.changeAccess( entry.getKey(), entry.getValue() );
            }
         }
      }
   }

   class TypedCaseDefaultDueOnConcern
         extends ConcernOf<TypedCase>
         implements TypedCase
   {
      @This
      DueOn dueOn;

      public void changeCaseType( @Optional CaseType newCaseType )
      {
         next.changeCaseType( newCaseType );

         if (newCaseType != null)
         {
            // If no due on is set, then set it to "now" plus the given number of days
            DefaultDaysToComplete.Data defaultDaysToComplete = (DefaultDaysToComplete.Data) newCaseType;
            if (defaultDaysToComplete.defaultDaysToComplete().get() > 0)
            {
               Calendar now = Calendar.getInstance();
               now.add(Calendar.DAY_OF_MONTH,defaultDaysToComplete.defaultDaysToComplete().get() );
               dueOn.defaultDueOn(now.getTime());
            }
         }
      }
   }

   class TypedCaseDefaultObligatoryPriorityConcern
      extends ConcernOf<TypedCase>
      implements TypedCase
   {

      @This
      CasePriority priority;

      @Structure
      Module module;

      public void changeCaseType( @Optional CaseType newCaseType )
      {
         next.changeCaseType( newCaseType );

         if (newCaseType == null)
         {
            priority.changePriority( null );
         } else
         {
            Priority defaultPriority = ((PriorityOnCase.Data) newCaseType).defaultPriority().get();

            if (((CasePriority.Data) priority).casepriority().get() == null)
            {
               if (defaultPriority != null)
               {
                  // Set default casepriority if casepriority is missing and there is a
                  // default setting
                  priority.changePriority( ((PriorityOnCase.Data) newCaseType).defaultPriority().get() );

               } else if (((PriorityOnCase.Data) newCaseType).mandatory().get())
               {
                  // If default priority is missing and priority is mandatory
                  // then set the priority that is closest to middle...
                  Organizations organizations = module.unitOfWorkFactory().currentUnitOfWork()
                        .get( Organizations.class, OrganizationsEntity.ORGANIZATIONS_ID );
                  int priorityCount = ((Priorities.Data) ((Organizations.Data) organizations).organization().get())
                        .prioritys().count();

                  Query<Priority> query = module
                        .queryBuilderFactory()
                        .newQueryBuilder( Priority.class )
                        .where(
                              QueryExpressions.eq( QueryExpressions.templateFor( (PrioritySettings.Data.class) )
                                    .priority(), Math.round( priorityCount / 2 ) ) )
                        .newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

                  priority.changePriority( query.find() );
               }
            }
         }
      }
   }

   abstract class OwnableCaseAccessConcern
         extends ConcernOf<Ownable>
         implements Ownable
   {
      @This
      CaseAccess caseAccess;

      public void changeOwner( Owner owner )
      {
         next.changeOwner( owner );

         // Transfer settings for security from new owner to case
         for (Map.Entry<PermissionType, CaseAccessType> entry : ((CaseAccessDefaults.Data) owner).accessPermissionDefaults().get().entrySet())
         {
            caseAccess.changeAccess( entry.getKey(), entry.getValue() );
         }
      }
   }

   class RemovableConcern
         extends ConcernOf<Removable>
         implements Removable
   {

      @This
      Attachments.Data attachmentsData;

      @This
      Attachments attachments;

      @This
      FormAttachments formAttachments;

      @This
      Conversations conversations;

      @This
      FormAttachments.Data formAttachmentsData;

      @This
      SubCase.Data subCase;

      @This
      SubCases.Data subCases;

      @This
      Notes.Data notes;

      @This
      Conversations.Data conversationsData;

      @This
      Case caze;

      @This
      Unread unread;

      @Structure
      Qi4j api;

      public boolean removeEntity()
      {
         for (Attachment attachment : attachmentsData.attachments().toList())
         {
            attachment.removeEntity();
         }

         for (Attachment attachment : formAttachmentsData.formAttachments().toList())
         {
            attachment.removeEntity();
         }

         for( Conversation conversation : conversationsData.conversations().toList() )
         {
            conversation.removeEntity();
         }

         for (Case childCase : subCases.subCases().toList())
         {
            childCase.removeEntity();
         }

         if( notes.notes().get() != null )
         {
            notes.notes().get().removeEntity();
         }
         return next.removeEntity();
      }

      public boolean reinstate()
      {
         for (Attachment attachment : attachmentsData.attachments().toList())
         {
            attachment.reinstate();
         }

         for (Attachment attachment : formAttachmentsData.formAttachments().toList())
         {
            attachment.reinstate();
         }

         for( Conversation conversation : conversationsData.conversations().toList() )
         {
            conversation.reinstate();
         }

         for (Case childCase : subCases.subCases().toList())
         {
            childCase.reinstate();
         }

         if( notes.notes().get() != null )
         {
            notes.notes().get().reinstate();
         }

         unread.setUnread( true );
         return next.reinstate();
      }

      public void deleteEntity()
      {
         for (Attachment attachment : attachmentsData.attachments().toList())
         {
            attachments.removeAttachment( attachment );
         }

         for (Attachment attachment : formAttachmentsData.formAttachments().toList())
         {
            formAttachments.removeFormAttachment( attachment );
         }

         for( Conversation conversation : conversationsData.conversations().toList())
         {
            conversations.removeConversation( conversation );
         }

         if (subCase.parent().get() != null)
            subCase.parent().get().removeSubCase( api.dereference( caze ) );

         for (Case childCase : subCases.subCases().toList())
         {
            caze.removeSubCase( childCase );
            childCase.deleteEntity();
         }

         if( notes.notes().get() != null )
         {
            notes.notes().get().deleteEntity();
         }
         next.deleteEntity();
      }
   }

   abstract class CaseLogCaseEntitySideEffect
         extends SideEffectOf<CaseEntity>
         implements CaseEntity
   {
      @This
      CaseLoggable.Data caseLoggable;

      public void assignTo( Assignee assignee )
      {
         caseLoggable.caselog().get().addTypedEntry( "{assigned,assignee=" + ((Describable) assignee).getDescription() + "}", CaseLogEntryTypes.system );
      }

      public void unassign()
      {
         caseLoggable.caselog().get().addTypedEntry( "{unassigned}", CaseLogEntryTypes.system );
      }

      public void open()
      {
         caseLoggable.caselog().get().addTypedEntry( "{opened}", CaseLogEntryTypes.system );
      }

      public void close()
      {
         caseLoggable.caselog().get().addTypedEntry( "{closed}", CaseLogEntryTypes.system );
      }

      public void onHold()
      {
         caseLoggable.caselog().get().addTypedEntry( "{paused}", CaseLogEntryTypes.system );
      }

      public void reopen()
      {
         caseLoggable.caselog().get().addTypedEntry( "{reopened}", CaseLogEntryTypes.system );
      }

      public void resume()
      {
         caseLoggable.caselog().get().addTypedEntry( "{resumed}", CaseLogEntryTypes.system );
      }

      public void resolve( Resolution resolution )
      {
         caseLoggable.caselog().get().addTypedEntry( "{resolved,resolution=" + resolution.getDescription() + "}", CaseLogEntryTypes.system );
      }

      public void changeCaseType( @Optional CaseType newCaseType )
      {
         caseLoggable.caselog().get().addTypedEntry( newCaseType != null ? "{changedCaseType,casetype=" + newCaseType.getDescription() + "}"
               : "{removedCaseType}", CaseLogEntryTypes.system );
      }

      public void changeOwner( Owner owner )
      {
         caseLoggable.caselog().get().addTypedEntry( "{changedOwner,owner=" + ((Project) owner).getDescription() + "}", CaseLogEntryTypes.system );
      }

      public void restrict()
      {
         caseLoggable.caselog().get().addTypedEntry( "{restrict}", CaseLogEntryTypes.system );
      }
      
      public void unrestrict()
      {
         caseLoggable.caselog().get().addTypedEntry( "{unrestrict}", CaseLogEntryTypes.system );
      }
   }

   abstract class UpdateSearchableFormsSideEffect
      extends SideEffectOf<SubmittedForms>
      implements SubmittedForms
   {
      @This SearchableForms searchableForms;

      public SubmittedFormValue submitForm(FormDraft formSubmission, Submitter submitter)
      {
         SubmittedFormValue submittedForm = result.submitForm( formSubmission, submitter );

         searchableForms.updateSearchableFormValues();

         return submittedForm;
      }
   }
   
   abstract class CaseLogContactConcern
   extends ConcernOf<Contacts>
   implements Contacts
   {
      @This
      CaseLoggable.Data caseLoggable;

      @This
      Contacts.Data contacts;
      
      public void addContact( ContactDTO newContact )
      {
         next.addContact( newContact );
         if (caseLoggable.caselog().get() != null)
         {
            caseLoggable.caselog().get().addTypedEntry( "{addContact}", CaseLogEntryTypes.contact);
         }
      }
      
      public void updateContact( int index, ContactDTO contact ){
         next.updateContact( index, contact );
         caseLoggable.caselog().get().addTypedEntry( "{updateContact,name=" + contact.name().get()+"}" , CaseLogEntryTypes.contact);
      }

      public void deleteContact( int index ){
         caseLoggable.caselog().get().addTypedEntry( "{deleteContact,name=" + contacts.contacts().get().get( index ).name().get()+"}" , CaseLogEntryTypes.contact);
         next.deleteContact( index );
      }
   }
   
   abstract class CaseLogConversationConcern
   extends ConcernOf<Conversations>
   implements Conversations
   {
      @This
      CaseLoggable.Data caseLoggable;

      public Conversation createConversation(String topic, Creator creator)
      {
         Conversation conversation = next.createConversation( topic, creator );
         caseLoggable.caselog().get().addTypedEntry( "{createConversation,topic=" + topic + "}" , CaseLogEntryTypes.conversation);
         return conversation;
      }
   }
   
   abstract class CaseLogAttachmentConcern
   extends ConcernOf<Attachments>
   implements Attachments
   {
      @This
      CaseLoggable.Data caseLoggable;

      public Attachment createAttachment(String uri) throws URISyntaxException
      {
         Attachment attachment = next.createAttachment( uri );
         caseLoggable.caselog().get().addTypedEntry( "{createAttachment}" , CaseLogEntryTypes.attachment);
         return attachment;
      }

      public void addAttachment(Attachment attachment)
      {
         next.addAttachment( attachment );
         caseLoggable.caselog().get().addTypedEntry( "{addAttachment,description=" + ((AttachedFile.Data)attachment).name().get() + "}" , CaseLogEntryTypes.attachment);
      }

      public void removeAttachment(Attachment attachment)
      {
         String fileName = ((AttachedFile.Data)attachment).name().get();
         next.removeAttachment( attachment );
         caseLoggable.caselog().get().addTypedEntry( "{removeAttachment,description=" + fileName + "}" , CaseLogEntryTypes.attachment);
      }
   }

   abstract class CaseLogSubmittedFormsConcern
   extends ConcernOf<SubmittedForms>
   implements SubmittedForms
   {
      @This
      CaseLoggable.Data caseLoggable;

      public SubmittedFormValue submitForm(FormDraft formSubmission, Submitter submitter)
      {
         SubmittedFormValue submittedForm = next.submitForm( formSubmission, submitter );
         caseLoggable.caselog().get().addTypedEntry( "{submitForm,description=" + formSubmission.getFormDraftValue().description().get() + "}" , CaseLogEntryTypes.form);
         return submittedForm;
      }
   }

   abstract class EmailAccesspointSideEffect
         extends SideEffectOf<CaseEntity>
         implements CaseEntity
   {

      @Structure
      Module module;

      @This
      Case caze;
      
      @This
      Origin origin;
      
      public void open()
      {
         if (origin.accesspoint().get() != null &&
               !Strings.empty( origin.accesspoint().get().emailTemplates().get().get( "received" ) ) )
         {
            // Switch to administrator user and send confirmation message
            UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
            UserEntity administrator = uow.get( UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME );
            RoleMap.current().set( administrator );

            Conversations.Data conversationsData = (Conversations.Data) caze;
            for (Conversation conversation : conversationsData.conversations())
            {
               if (conversation.isParticipant( (ConversationParticipant) caze.createdBy().get() ))
               {
                  Map<String,String> variables = new HashMap<String, String>(  );
                  variables.put( "caseid", ((CaseId.Data) caze).caseId().get() );
                  variables.put( "subject", caze.getDescription() );

                  if ( noMailRestrictionPresent( caze.createdBy().get() ))
                  {
                     conversation.createMessage( Translator.translate("{received,caseid=" + ((CaseId.Data) caze).caseId().get() + ",subject=" + caze.getDescription() + "}", origin.accesspoint().get().emailTemplates().get(), variables  ),
                        MessageType.SYSTEM, administrator, false );
                  }
               }
            }
         }
      }

       private boolean noMailRestrictionPresent(final Creator creator)
       {
           MailRestrictions mailRestrictions = RoleMap.role(MailRestrictions.class);
           MailRestriction mailRestriction = null;

           if( creator instanceof EmailUserEntity )
           {
               mailRestriction = Iterables.first(Iterables.filter(new Specification<MailRestriction>()
               {
                   @Override
                   public boolean satisfiedBy(MailRestriction item)
                   {
                       return ((Identity)creator).identity().get().equals( "email:" + item.getDescription() );
                   }
               },mailRestrictions.getMailRestrictions()));

           }
           return mailRestriction == null;
       }

       public void close()
      {
         if (origin.accesspoint().get() != null&&
               !Strings.empty( origin.accesspoint().get().emailTemplates().get().get( "closed" ) ) )
         {
            // Switch to administrator user and send close message
            UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
            UserEntity administrator = uow.get( UserEntity.class, UserEntity.ADMINISTRATOR_USERNAME );
            RoleMap.current().set( administrator );

            Conversations.Data conversationsData = (Conversations.Data) caze;
            for (Conversation conversation : conversationsData.conversations())
            {
               if (conversation.isParticipant( (ConversationParticipant) caze.createdBy().get() ))
               {
                  Map<String,String> variables = new HashMap<String, String>(  );
                  variables.put( "caseid", ((CaseId.Data) caze).caseId().get() );
                  variables.put( "subject", caze.getDescription() );

                   if ( noMailRestrictionPresent( caze.createdBy().get() ))
                   {
                        conversation.createMessage( Translator.translate( "{closed,caseid=" + ((CaseId.Data) caze).caseId().get() + ",subject=" + caze.getDescription() + "}", origin.accesspoint().get().emailTemplates().get(), variables ),
                            MessageType.SYSTEM, administrator, false );
                   }
               }
            }
         }
      }

      
   }


   abstract class AssignableConcern
      extends ConcernOf<Assignable>
      implements Assignable
   {
      @This
      Conversations conversations;
      
      @This
      Conversations.Data conversationsData;

      @This
      Unread unread;
      
      public void assignTo( Assignee assignee )
      {
         next.assignTo( assignee );
         
         if( conversations.hasConversations() )
         {
            Conversation conversation = conversationsData.conversations().get( 0 );
            conversation.addParticipant( (ConversationParticipant)assignee );
         }
      }

      public void unassign()
      {
         next.unassign();
         unread.setUnread( true );
      }
   }

   abstract class UnreadConcern
      extends ConcernOf<Unread>
      implements Unread
   {
      @This
      SubmittedForms submittedForms;

      @This
      Conversations conversations;

      public boolean isUnread()
      {
         return conversations.hasUnreadConversation() | submittedForms.hasUnreadForm() | next.isUnread() ;
      }
   }

   abstract class OwnableUnreadConcern
      extends ConcernOf<Ownable>
      implements Ownable
   {
      @This
      Unread unread;

      public void changeOwner( Owner owner )
      {
         next.changeOwner( owner );
         unread.setUnread( true );
      }
   }

   abstract class StatusConcern
      extends ConcernOf<Status>
      implements Status
   {
      @This
      Unread unread;

      public void reopen()
      {
         next.reopen();
         unread.setUnread( true );
      }
   }
}
