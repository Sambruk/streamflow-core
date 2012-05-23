/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.domain.entity.project;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.structure.Module;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.InboxQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.gtd.IdGenerator;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessDefaults;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.DueOnNotificationSettings;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;

/**
 * JAVADOC
 */
@SideEffects(ProjectEntity.RemoveMemberSideEffect.class)
@Mixins({ProjectEntity.ProjectIdGeneratorMixin.class})
@Concerns(ProjectEntity.RemovableConcern.class)
public interface ProjectEntity
        extends DomainEntity,

        // Interactions
        IdGenerator,

        // Structure
        Project,
        OwningOrganizationalUnit,

        // Data
        CaseAccessDefaults.Data,
        Members.Data,
        Describable.Data,
        OwningOrganizationalUnit.Data,
        Ownable.Data,
        Ownable.Events,
        Forms.Data,
        Labels.Data,
        SelectedLabels.Data,
        CaseTypes.Data,
        Removable.Data,
        SelectedCaseTypes.Data,
        Filters.Data,
        Filters.Events,
        DueOnNotificationSettings.Data,
        DueOnNotificationSettings.Events,

        // Queries
        AssignmentsQueries,
        InboxQueries,
        ProjectLabelsQueries
{
   class ProjectIdGeneratorMixin
           implements IdGenerator
   {
      @This
      OwningOrganizationalUnit.Data state;

      public void assignId(CaseId aCase)
      {
         Organization organization = ((OwningOrganization) state.organizationalUnit().get()).organization().get();
         ((IdGenerator) organization).assignId(aCase);
      }
   }

   abstract class RemoveMemberSideEffect
           extends SideEffectOf<Members>
           implements Members
   {
      @This
      AssignmentsQueries assignments;

      @Structure
      Module module;

      public void removeMember(Member member)
      {
         // Get all active cases in a project for a particular user and unassign.
         for (Assignable caze : assignments.assignments((Assignee) member, null).newQuery(module.unitOfWorkFactory().currentUnitOfWork()))
         {
            caze.unassign();
         }
      }
   }

   abstract class RemovableConcern
           extends ConcernOf<Removable>
           implements Removable
   {
      @This
      Identity id;

      @This
      Members members;

      @This
      InboxQueries inbox;

      @This
      AssignmentsQueries assignments;

      public boolean removeEntity()
      {
         if (inbox.inboxHasActiveCases()
                 || assignments.assignmentsHaveActiveCases())
         {
            throw new IllegalStateException("Cannot remove project with OPEN cases.");

         } else
         {
            members.removeAllMembers();
            return next.removeEntity();
         }
      }
   }
}
