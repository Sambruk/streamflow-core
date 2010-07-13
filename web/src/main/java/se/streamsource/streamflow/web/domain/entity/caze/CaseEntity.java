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
import se.streamsource.streamflow.web.domain.interaction.gtd.DueOn;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Status;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.casetype.Resolvable;
import se.streamsource.streamflow.web.domain.structure.casetype.TypedCase;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.caze.Closed;
import se.streamsource.streamflow.web.domain.structure.caze.Contacts;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmissions;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.label.Labelable;

/**
 * This represents a single Case in the system
 */
@SideEffects({AssignIdSideEffect.class, StatusClosedSideEffect.class})
public interface CaseEntity
      extends Case,

      // Interactions
      Assignable,
      Assignable.Data,
      Commentable,
      Commentable.Data,
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
      Closed,
      Attachments.Data,
      Contacts.Data,
      Labelable.Data,
      Removable.Data,
      Resolvable.Data,
      FormSubmissions.Data,
      SubmittedForms.Data,
      TypedCase.Data,

      // Queries
      SubmittedFormsQueries,
      FormSubmissionsQueries,
      CaseLabelsQueries,
      CaseTypeQueries,

      DomainEntity
{
}
