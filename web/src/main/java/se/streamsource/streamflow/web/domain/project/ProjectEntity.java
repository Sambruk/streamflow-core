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

package se.streamsource.streamflow.web.domain.project;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.domain.roles.Removable;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.web.domain.DomainEntity;
import se.streamsource.streamflow.web.domain.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.task.Assignments;
import se.streamsource.streamflow.web.domain.task.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.task.Delegations;
import se.streamsource.streamflow.web.domain.task.DelegationsQueries;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.InboxQueries;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.TaskId;
import se.streamsource.streamflow.web.domain.task.WaitingFor;
import se.streamsource.streamflow.web.domain.task.WaitingForQueries;
import se.streamsource.streamflow.web.domain.tasktype.SelectedTaskTypes;

/**
 * JAVADOC
 */
@Mixins(ProjectEntity.ProjectIdGeneratorMixin.class)
@Concerns(ProjectEntity.RemovableConcern.class)
public interface ProjectEntity
      extends DomainEntity,
      Project,

      // Data
      Inbox.Data,
      Assignments.Data,
      Delegations.Data,
      WaitingFor.Data,
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
      WaitingForQueries
{
   class ProjectIdGeneratorMixin
         implements IdGenerator
   {
      @This
      OwningOrganizationalUnit.Data state;

      public void assignId( TaskId task )
      {
         ((OwningOrganization) state.organizationalUnit().get()).organization().get().assignId( task );
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

      @This
      Delegations delegations;

      public boolean removeEntity()
      {
         if (inbox.inboxHasActiveTasks()
               || assignments.assignmentsHaveActiveTasks()
               || waitingFor.hasActiveOrDoneAndUnreadTasks())
         {
            throw new IllegalStateException( "Cannot remove project with ACTIVE tasks." );

         } else
         {
            for (TaskDTO taskDTO : delegationsQueries.delegationsTasks().tasks().get())
            {
               delegations.reject( uowf.currentUnitOfWork().get( Task.class, taskDTO.task().get().identity() ) );
            }
            members.removeAllMembers();
            return next.removeEntity();
         }
      }
   }
}
