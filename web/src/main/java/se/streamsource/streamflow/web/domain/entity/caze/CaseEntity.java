/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.domain.entity.caze;

import org.qi4j.api.Qi4j;
import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Notable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.AssignIdSideEffect;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.interaction.security.Authorization;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccess;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessDefaults;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessType;
import se.streamsource.streamflow.web.domain.interaction.security.PermissionType;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.attachment.FormAttachments;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.DefaultDaysToComplete;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolution;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolvable;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.Closed;
import se.streamsource.streamflow.web.domain.structure.caze.Contacts;
import se.streamsource.streamflow.web.domain.structure.caze.History;
import se.streamsource.streamflow.web.domain.structure.caze.SubCase;
import se.streamsource.streamflow.web.domain.structure.caze.SubCases;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;
import se.streamsource.streamflow.web.domain.structure.form.SearchableForms;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.form.Submitter;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * This represents a single Case in the system
 */
@SideEffects({AssignIdSideEffect.class, StatusClosedSideEffect.class, CaseEntity.HistorySideEffect.class, CaseEntity.UpdateSearchableFormsSideEffect.class})
@Concerns({CaseEntity.RemovableConcern.class, CaseEntity.TypedCaseAccessConcern.class, CaseEntity.TypedCaseDefaultDueOnConcern.class, CaseEntity.OwnableCaseAccessConcern.class})
@Mixins(CaseEntity.AuthorizationMixin.class)
public interface CaseEntity
      extends Case,

      // Interactions
      Assignable.Events,
      Assignable.Data,
      Describable.Data,
      DueOn.Data,
      Notable.Data,
      Ownable.Data,
      CaseId.Data,
      Status.Events,
      Status.Data,
      Conversations.Data,
      CaseAccess.Data,

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

   abstract class RemovableConcern
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
      FormAttachments.Data formAttachmentsData;

      @This
      SubCase.Data subCase;

      @This
      SubCases.Data subCases;

      @This
      Case caze;

      @Structure
      Qi4j api;

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

         if (subCase.parent().get() != null)
            subCase.parent().get().removeSubCase( api.dereference( caze ) );

         for (Case childCase : subCases.subCases().toList())
         {
            caze.removeSubCase( childCase );
            childCase.deleteEntity();
         }

         next.deleteEntity();
      }
   }

   abstract class HistorySideEffect
         extends SideEffectOf<CaseEntity>
         implements CaseEntity
   {
      @This
      History history;

      public void changeDescription( @Optional String newDescription )
      {
         history.getHistory().changeDescription( newDescription );
      }

      public void assignTo( Assignee assignee )
      {
         history.addHistoryComment( "{assigned,assignee=" + ((Describable) assignee).getDescription() +"}", RoleMap.role( ConversationParticipant.class ) );
      }

      public void unassign()
      {
         history.addHistoryComment( "{unassigned}", RoleMap.role( ConversationParticipant.class ) );
      }

      public void open()
      {
         history.addHistoryComment( "{opened}", RoleMap.role( ConversationParticipant.class ) );
      }

      public void close()
      {
         history.addHistoryComment( "{closed}", RoleMap.role( ConversationParticipant.class ) );
      }

      public void onHold()
      {
         history.addHistoryComment( "{paused}", RoleMap.role( ConversationParticipant.class ) );
      }

      public void reopen()
      {
         history.addHistoryComment( "{reopened}", RoleMap.role( ConversationParticipant.class ) );
      }

      public void resume()
      {
         history.addHistoryComment( "{resumed}", RoleMap.role( ConversationParticipant.class ) );
      }

      public void resolve( Resolution resolution )
      {
         history.addHistoryComment( "{resolved,resolution=" + resolution.getDescription()+"}", RoleMap.role( ConversationParticipant.class ) );
      }

      public void changeCaseType( @Optional CaseType newCaseType )
      {
         history.addHistoryComment( newCaseType != null ? "{changedCaseType,casetype=" + newCaseType.getDescription() +"}"
               : "{removedCaseType}", RoleMap.role( ConversationParticipant.class ) );
      }

      public void changeOwner( Owner owner )
      {
         history.addHistoryComment( "{changedOwner,owner=" + ((Project)owner).getDescription() +"}"
               , RoleMap.role( ConversationParticipant.class ) );
      }

      public void createSubCase()
      {
         history.addHistoryComment( "{createdSubCase}", RoleMap.role( ConversationParticipant.class ) );
      }
   }

   abstract class UpdateSearchableFormsSideEffect
      extends SideEffectOf<SubmittedForms>
      implements SubmittedForms
   {
      @This SearchableForms searchableForms;

      public void submitForm(FormDraft formSubmission, Submitter submitter)
      {
         result.submitForm(formSubmission, submitter);

         searchableForms.updateSearchableFormValues();
      }
   }
}
