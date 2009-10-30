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

package se.streamsource.streamflow.web.resource.users.workspace.projects.forms;

import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.web.domain.form.FormsQueries;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * /workspace/projects/{project}/forms
 */
public class WorkspaceProjectFormDefinitionsServerResource
        extends CommandQueryServerResource
{
    public ListValue applicableFormDefinitionList()
    {
        String identity = getRequest().getAttributes().get("project").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        FormsQueries forms = uow.get( FormsQueries.class, identity);

        return forms.applicableFormDefinitionList();
    }

    public ListValue nonApplicableFormDefinitionList()
    {
        String identity = getRequest().getAttributes().get("project").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        FormsQueries forms = uow.get( FormsQueries.class, identity);

        return forms.nonApplicableFormDefinitionList();
    }
}