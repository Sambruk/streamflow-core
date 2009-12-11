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

package se.streamsource.streamflow.web.resource.task;

import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import org.restlet.representation.Variant;
import org.restlet.data.MediaType;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * JAVADOC
 */
public class TaskServerResource
    extends CommandQueryServerResource
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    public TaskServerResource()
    {
        setNegotiated(true);
        getVariants().add(new Variant( MediaType.APPLICATION_JSON));
    }

    public ListValue possibleprojects()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        TaskEntity task = uow.get(TaskEntity.class, getRequest().getAttributes().get("task").toString());

        return task.possibleProjects();
    }
}
