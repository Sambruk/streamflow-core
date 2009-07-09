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

package se.streamsource.streamflow.web.resource.users.shared.user.task.general;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.BaseServerResource;

/**
 * Mapped to:
 * /users/{user}/shared/user/{view}/{task}/general
 */
public class SharedUserTaskGeneralServerResource
    extends BaseServerResource
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    public SharedUserTaskGeneralServerResource()
    {
        setNegotiated(true);
        getVariants().put(Method.ALL, MediaType.APPLICATION_JSON);
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Get general task information"));
        ValueBuilder<TaskGeneralDTO> builder = vbf.newValueBuilder(TaskGeneralDTO.class);
        TaskEntity task = uow.get(TaskEntity.class, getRequest().getAttributes().get("task").toString());
        builder.prototype().description().set(task.description().get());
        builder.prototype().note().set(task.note().get());
        builder.prototype().creationDate().set(task.createdOn().get());
        builder.prototype().taskId().set(task.taskId().get());
        uow.discard();

        return new StringRepresentation(builder.newInstance().toJSON(), MediaType.APPLICATION_JSON);
    }

    @Override
    protected Representation put(Representation representation, Variant variant) throws ResourceException
    {
        UnitOfWork uow = uowf.newUnitOfWork(UsecaseBuilder.newUsecase("Update general task information"));
        try
        {
            TaskGeneralDTO updated = vbf.newValueFromJSON(TaskGeneralDTO.class, representation.getText());
            TaskEntity task = uow.get(TaskEntity.class, getRequest().getAttributes().get("task").toString());
            task.describe(updated.description().get());
            task.changeNote(updated.note().get());
            uow.complete();
        } catch (Exception e)
        {
            e.printStackTrace();
            uow.discard();
        }

        return null;
    }
}
