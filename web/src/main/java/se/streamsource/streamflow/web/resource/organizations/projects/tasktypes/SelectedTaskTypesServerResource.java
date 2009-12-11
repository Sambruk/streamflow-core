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

package se.streamsource.streamflow.web.resource.organizations.projects.tasktypes;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.tasktype.SelectedTaskTypes;
import se.streamsource.streamflow.web.domain.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.tasktype.TaskTypes;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/organizationalunits/{ou}/projects/{tasktypes}/tasktypes
 */
public class SelectedTaskTypesServerResource
        extends CommandQueryServerResource
{
    @Structure
    UnitOfWorkFactory uowf;

    @Structure
    ValueBuilderFactory vbf;

    public ListValue selectedtasktypes()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("tasktypes");
        SelectedTaskTypes.Data taskTypes = uow.get( SelectedTaskTypes.Data.class, id);

        return new ListValueBuilder(vbf).addDescribableItems( taskTypes.selectedTaskTypes() ).newList();
    }

    public ListValue possibletasktypes()
    {
        UnitOfWork uow = uowf.currentUnitOfWork();
        String id = (String) getRequest().getAttributes().get("tasktypes");
        String organization = (String) getRequest().getAttributes().get("organization");
        SelectedTaskTypes.Data selectedLabels = uow.get( SelectedTaskTypes.Data.class, id);
        TaskTypes.Data taskTypes = uow.get( TaskTypes.Data.class, organization);

        return selectedLabels.possibleTaskTypes( taskTypes.taskTypes() );
    }

    public void addtasktype( EntityReferenceDTO taskTypeDTO) throws ResourceException
    {
        String identity = getRequest().getAttributes().get("tasktypes").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        SelectedTaskTypes taskTypes = uow.get(SelectedTaskTypes.class, identity);
        TaskType taskType = uow.get( TaskType.class, taskTypeDTO.entity().get().identity());

        taskTypes.addSelectedTaskType( taskType );
    }
}