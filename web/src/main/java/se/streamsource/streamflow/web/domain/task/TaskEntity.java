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
import se.streamsource.streamflow.web.domain.task.Contacts;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.domain.roles.Notable;
import se.streamsource.streamflow.web.domain.DomainEntity;
import se.streamsource.streamflow.web.domain.comment.Commentable;
import se.streamsource.streamflow.web.domain.label.Labelable;
import se.streamsource.streamflow.web.domain.project.AssignTaskIdSideEffect;

/**
 * JAVADOC
 */
@SideEffects(AssignTaskIdSideEffect.class)
public interface TaskEntity
        extends Task,
        // State
        Assignable.AssignableState,
        Commentable.CommentableState,
        Contacts.ContactsState,
        CreatedOn.CreatedOnState,
        Delegatable.DelegatableState,
        Describable.DescribableState,
        DueOn.DueOnState,
        Labelable.LabelableState,
        Notable.NotableState,
        Ownable.OwnableState,
        TaskStatus.TaskStatusState,
        TaskId.TaskIdState,
        DomainEntity
{
}
