/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.entity.task;

import org.qi4j.api.concern.Concerns;
import org.qi4j.api.sideeffect.SideEffects;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Notable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.form.FormSubmissionsQueries;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.interaction.comment.Commentable;
import se.streamsource.streamflow.web.domain.interaction.gtd.AssignIdSideEffect;
import se.streamsource.streamflow.web.domain.interaction.gtd.Assignable;
import se.streamsource.streamflow.web.domain.interaction.gtd.CompletableId;
import se.streamsource.streamflow.web.domain.interaction.gtd.Delegatable;
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmissions;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;
import se.streamsource.streamflow.web.domain.structure.task.Contacts;
import se.streamsource.streamflow.web.domain.structure.task.Task;
import se.streamsource.streamflow.web.domain.structure.tasktype.TypedTask;

/**
 * JAVADOC
 */
@SideEffects(AssignIdSideEffect.class)
@Concerns( OwnershipConcern.class)
public interface TaskEntity
      extends Task,

      // Interactions
      Assignable,
      Assignable.Data,
      Commentable,
      Commentable.Data,
      Delegatable,
      Delegatable.Data,
      Describable,
      Describable.Data,
      DueOn,
      DueOn.Data,
      Notable,
      Notable.Data,
      Ownable,
      Ownable.Data,
      PossibleActions,
      CompletableId,
      CompletableId.Data,
      Status,
      Status.Data,
      CreatedOn,
      Conversations.Data,      

      // Structure
      Contacts.Data,
      Labelable.Data,
      Removable.Data,
      FormSubmissions.Data,
      SubmittedForms.Data,
      TypedTask.Data,

      // Queries
      SubmittedFormsQueries,
      FormSubmissionsQueries,
      TaskLabelsQueries,
      TaskTypeQueries,

      DomainEntity
{
}
