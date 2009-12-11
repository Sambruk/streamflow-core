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

package se.streamsource.streamflow.web.domain.task;

import org.qi4j.api.sideeffect.SideEffects;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.domain.roles.Notable;
import se.streamsource.streamflow.web.domain.DomainEntity;
import se.streamsource.streamflow.web.domain.comment.Commentable;
import se.streamsource.streamflow.web.domain.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.label.Labelable;
import se.streamsource.streamflow.web.domain.project.AssignTaskIdSideEffect;
import se.streamsource.streamflow.web.domain.tasktype.TaskTypeQueries;
import se.streamsource.streamflow.web.domain.tasktype.TypedTask;

/**
 * JAVADOC
 */
@SideEffects(AssignTaskIdSideEffect.class)
public interface TaskEntity
        extends Task,
        // Data
        Assignable.Data,
        Commentable.Data,
        Contacts.Data,
        CreatedOn,
        Delegatable.Data,
        Describable.Data,
        DueOn.Data,
        Labelable.Data,
        Notable.Data,
        Ownable.Data,
        SubmittedForms.Data,
        TaskStatus.Data,
        TaskId.Data,
        TaskQueries,
        TypedTask.Data,

        // Queries
        SubmittedFormsQueries,
        TaskLabelsQueries,
        TaskTypeQueries,

        DomainEntity
{
}
