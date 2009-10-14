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

package se.streamsource.streamflow.web.resource.task.general;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.domain.roles.Notable;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.resource.roles.DateDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;
import se.streamsource.streamflow.web.domain.label.Label;
import se.streamsource.streamflow.web.domain.task.DueOn;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /task/{task}/general
 */
public class TaskGeneralServerResource
        extends CommandQueryServerResource
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    public TaskGeneralServerResource()
    {
        setNegotiated(true);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Get general task information"));
        ValueBuilder<TaskGeneralDTO> builder = vbf.newValueBuilder(TaskGeneralDTO.class);
        TaskEntity task = uow.get(TaskEntity.class, getRequest().getAttributes().get("task").toString());
        builder.prototype().description().set(task.description().get());

        ValueBuilder<ListValue> labelsBuilder = vbf.newValueBuilder( ListValue.class);
        ValueBuilder<ListItemValue> labelsItemBuilder = vbf.newValueBuilder( ListItemValue.class);
		for (Label label : task.labels())
		{
            labelsItemBuilder.prototype().entity().set( EntityReference.getEntityReference(label ));
            labelsItemBuilder.prototype().description().set( label.getDescription());
            labelsBuilder.prototype().items().get().add( labelsItemBuilder.newInstance() );
		}

		builder.prototype().labels().set(labelsBuilder.newInstance());
        builder.prototype().note().set(task.note().get());
        builder.prototype().creationDate().set(task.createdOn().get());
        builder.prototype().taskId().set(task.taskId().get());
        builder.prototype().dueOn().set(task.dueOn().get());
        uow.discard();

        return new StringRepresentation(builder.newInstance().toJSON(), MediaType.APPLICATION_JSON);
    }

    public void describe(StringDTO stringValue)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        Describable describable = uowf.currentUnitOfWork().get(Describable.class, taskId);
        describable.changeDescription(stringValue.string().get());
    }

    public void changeNote(StringDTO noteValue)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        Notable notable = uowf.currentUnitOfWork().get(Notable.class, taskId);
        notable.changeNote(noteValue.string().get());
    }

    public void changeDueOn(DateDTO dueOnValue)
    {
        String taskId = (String) getRequest().getAttributes().get("task");
        DueOn dueOn = uowf.currentUnitOfWork().get(DueOn.class, taskId);
        dueOn.dueOn(dueOnValue.date().get());
    }
}
