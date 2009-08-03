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

package se.streamsource.streamflow.web.resource.users.workspace;

import se.streamsource.streamflow.web.resource.CommandQueryServerResource;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.domain.task.Inbox;
import se.streamsource.streamflow.web.domain.task.Task;
import se.streamsource.streamflow.web.domain.task.Assignee;
import se.streamsource.streamflow.web.domain.label.Label;
import se.streamsource.streamflow.resource.task.TaskListDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.NewTaskCommand;
import se.streamsource.streamflow.resource.label.LabelDTO;
import se.streamsource.streamflow.resource.label.LabelListDTO;
import org.qi4j.api.query.Query;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;

import java.util.List;

/**
 * JAVADOC
 */
public class AbstractTaskListServerResource
    extends CommandQueryServerResource
{

    protected  <T extends TaskListDTO, V extends TaskDTO> T buildTaskList(String id, 
                                                         Query<TaskEntity> inboxQuery,
                                                         Class<V> taskClass,
                                                         Class<T> taskListClass)
    {
        ValueBuilder<V> builder = vbf.newValueBuilder(taskClass);
        TaskDTO prototype = builder.prototype();
        ValueBuilder<T> listBuilder = vbf.newValueBuilder(taskListClass);
        T t = listBuilder.prototype();
        Property<List<V>> property = t.tasks();
        List<V> list = property.get();
        EntityReference ref = EntityReference.parseEntityReference(id);
        ValueBuilder<LabelDTO> labelBuilder = vbf.newValueBuilder(LabelDTO.class);
        LabelDTO labelPrototype = labelBuilder.prototype();
        for (TaskEntity task : inboxQuery)
        {
            buildTask(prototype, ref, labelBuilder, labelPrototype, task);

            list.add(builder.newInstance());
        }
        return listBuilder.newInstance();
    }

    protected <T extends TaskListDTO> void buildTask(TaskDTO prototype, EntityReference ref, ValueBuilder<LabelDTO> labelBuilder, LabelDTO labelPrototype, TaskEntity task)
    {
        prototype.owner().set(ref);
        prototype.task().set(EntityReference.getEntityReference(task));
        prototype.creationDate().set(task.createdOn().get());
        prototype.description().set(task.description().get());
        prototype.status().set(task.status().get());
        prototype.isRead().set(task.isRead().get());

        ValueBuilder<LabelListDTO> labelListBuilder = vbf.newValueBuilder(LabelListDTO.class);
        List<LabelDTO> labelList = labelListBuilder.prototype().labels().get();
        for (Label label : task.labels())
        {
            labelPrototype.label().set(EntityReference.getEntityReference(label));
            labelPrototype.description().set(label.getDescription());
            labelList.add(labelBuilder.newInstance());
        }
        prototype.labels().set(labelListBuilder.newInstance());
    }


    protected void newTask(NewTaskCommand command, String inboxId, String assigneeId)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        Inbox inbox = uow.get(Inbox.class, inboxId);
        Task task = inbox.newTask();
        task.describe(command.description().get());
        task.changeNote(command.note().get());

        if (command.isCompleted().get())
        {
            Assignee assignee = uow.get(Assignee.class, assigneeId);
            inbox.completeTask(task, assignee);
        }
    }
}
