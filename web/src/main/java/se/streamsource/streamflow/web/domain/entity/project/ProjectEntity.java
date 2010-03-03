/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.entity.project;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.DelegationsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.Inbox;
import se.streamsource.streamflow.web.domain.entity.gtd.InboxQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.WaitingForQueries;
import se.streamsource.streamflow.web.domain.entity.label.PossibleLabelsQueries;
import se.streamsource.streamflow.web.domain.entity.task.TaskEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignee;
import se.streamsource.streamflow.web.domain.interaction.gtd.CompletableId;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatee;
import se.streamsource.streamflow.web.domain.interaction.gtd.IdGenerator;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.tasktype.SelectedTaskTypes;

/**
 * JAVADOC
 */
@SideEffects(ProjectEntity.RemoveMemberSideEffect.class)
@Mixins({ProjectEntity.ProjectIdGeneratorMixin.class, ProjectEntity.DelegateeMixin.class})
@Concerns(ProjectEntity.RemovableConcern.class)
public interface ProjectEntity
      extends DomainEntity,

      Inbox,

      // Interactions
      Describable,
      Delegatee,
      Owner,
      SelectedLabels,
      IdGenerator,
      SelectedTaskTypes,

      // Structure
      Members,
      Project,
      OwningOrganizationalUnit,
      Removable,

      // Data
      Inbox.Data,
      Members.Data,
      Describable.Data,
      OwningOrganizationalUnit.Data,
      SelectedLabels.Data,
      Removable.Data,
      SelectedTaskTypes.Data,

      // Queries
      AssignmentsQueries,
      DelegationsQueries,
      InboxQueries,
      WaitingForQueries,
      PossibleLabelsQueries,
      ProjectLabelsQueries
{
   class ProjectIdGeneratorMixin
         implements IdGenerator
   {
      @This
      OwningOrganizationalUnit.Data state;

      public void assignId( CompletableId completable )
      {
         Organization organization = ((OwningOrganization) state.organizationalUnit().get()).organization().get();
         ((IdGenerator)organization).assignId( completable );
      }
   }

   class DelegateeMixin
      implements Delegatee
   {
      @This Project project;

      public boolean isDelegatedTo( Delegatee delegatee )
      {
         return ((Member)delegatee).isMember( project );
      }
   }

   abstract class RemoveMemberSideEffect
      extends SideEffectOf<Members>
      implements Members
   {
      @This
      AssignmentsQueries assignments;

      @Structure
      UnitOfWorkFactory uowf;

      public void removeMember( Member member )
      {
         // Get all active tasks in a project for a particular user and unassign.
         for (Assignable task : assignments.assignments( (Assignee) member ).newQuery( uowf.currentUnitOfWork() ))
         {
            task.unassign();
         }
      }
   }

   abstract class RemovableConcern
         extends ConcernOf<Removable>
         implements Removable
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

      @This
      Identity id;

      @This
      Members members;

      @This
      InboxQueries inbox;

      @This
      AssignmentsQueries assignments;

      @This
      WaitingForQueries waitingFor;

      @This
      DelegationsQueries delegationsQueries;

      public boolean removeEntity()
      {
         if (inbox.inboxHasActiveTasks()
               || assignments.assignmentsHaveActiveTasks()
               || waitingFor.hasActiveOrDoneAndUnreadTasks())
         {
            throw new IllegalStateException( "Cannot remove project with ACTIVE tasks." );

         } else
         {
            for (Delegatable delegatable : delegationsQueries.delegations().newQuery( uowf.currentUnitOfWork() ))
            {
               uowf.currentUnitOfWork().get( TaskEntity.class, ((Identity)delegatable).identity().get() ).rejectDelegation();
            }
            members.removeAllMembers();
            return next.removeEntity();
         }
      }
   }
}
