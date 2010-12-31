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

package se.streamsource.streamflow.web.domain.entity.caze;

import org.qi4j.api.Qi4j;
import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Notable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;
import se.streamsource.streamflow.web.domain.interaction.security.*;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachment;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.attachment.FormAttachments;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolvable;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.caze.*;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.form.FormDrafts;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.User;

import java.util.Map;

/**
 * This represents a single Case in the system
 */
@SideEffects({AssignIdSideEffect.class, StatusClosedSideEffect.class})
@Concerns({CaseEntity.RemovableConcern.class, CaseEntity.TypedCaseAccessConcern.class, CaseEntity.OwnableCaseAccessConcern.class})
@Mixins(CaseEntity.AuthorizationMixin.class)
public interface CaseEntity
      extends Case,

      // Interactions
      Assignable.Data,
      Describable.Data,
      DueOn.Data,
      Notable.Data,
      Ownable.Data,
      CaseId.Data,
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
      TypedCase.Data,
      SubCases.Data,
      SubCase.Data,

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
      UnitOfWorkFactory uowf;

      public boolean hasPermission( String userId, String permission )
      {
         User actor = uowf.currentUnitOfWork().get( User.class, userId );

         switch (aCase.status().get())
         {
            case DRAFT:
            {
               // Creator has all permissions
               return aCase.createdBy().get().equals(actor);
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
      @This CaseAccess caseAccess;

      public void changeCaseType( @Optional CaseType newCaseType )
      {
         next.changeCaseType( newCaseType );

         // Transfer settings for security from new casetype to case
         for (Map.Entry<PermissionType, CaseAccessType> entry : ((CaseAccessDefaults.Data)newCaseType).accessPermissionDefaults().get().entrySet())
         {
            caseAccess.changeAccess( entry.getKey(), entry.getValue() );
         }
      }
   }

   abstract class OwnableCaseAccessConcern
      extends ConcernOf<Ownable>
      implements Ownable
   {
      @This CaseAccess caseAccess;

      public void changeOwner( Owner owner )
      {
         next.changeOwner( owner );

         // Transfer settings for security from new owner to case
         for (Map.Entry<PermissionType, CaseAccessType> entry : ((CaseAccessDefaults.Data)owner).accessPermissionDefaults().get().entrySet())
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

         for( Attachment attachment : formAttachmentsData.formAttachments().toList() )
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
}
