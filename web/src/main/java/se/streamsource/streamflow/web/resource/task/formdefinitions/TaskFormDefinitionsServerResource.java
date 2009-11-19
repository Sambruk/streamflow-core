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

package se.streamsource.streamflow.web.resource.task.formdefinitions;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.form.FormsQueries;
import se.streamsource.streamflow.web.domain.task.TaskQueries;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /tasks/{task}/formdefinitions/
 */
public class TaskFormDefinitionsServerResource
        extends CommandQueryServerResource
{
    @Structure
    ValueBuilderFactory vbf;

    public TaskFormDefinitionsServerResource()
    {
        setNegotiated(true);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    public ListValue applicableFormDefinitionList()
    {
        // find project identity is
        String taskId = getRequest().getAttributes().get("task").toString();
        UnitOfWork uow = uowf.currentUnitOfWork();

        TaskQueries taskQueries = uow.get(TaskQueries.class, taskId);

        ProjectEntity project = taskQueries.ownerProject();

        ListValue formsList;
        if (project != null)
        {
            FormsQueries forms = uow.get( FormsQueries.class, project.identity().get());
            formsList = forms.applicableFormDefinitionList();
        } else
        {
            formsList = vbf.newValue(ListValue.class);
        }
        return formsList;
    }

    @Override
    protected String getConditionalIdentityAttribute()
    {
        return "task";
    }
}