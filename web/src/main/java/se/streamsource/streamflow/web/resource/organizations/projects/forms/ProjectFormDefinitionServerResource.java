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

package se.streamsource.streamflow.web.resource.organizations.projects.forms;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.form.FormValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.form.FormEntity;
import se.streamsource.streamflow.web.domain.form.FormsQueries;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /organizations/{organization}/projects/{project}/forms/{index}
 */
public class ProjectFormDefinitionServerResource
        extends CommandQueryServerResource
{
    public FormValue form()
    {
        String identity = getRequest().getAttributes().get("project").toString();
        String index = getRequest().getAttributes().get("index").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        FormsQueries forms = uow.get( FormsQueries.class, identity);

        checkPermission(forms);

        List<ListItemValue> itemValues = forms.applicableFormDefinitionList().items().get();

        ListItemValue value = itemValues.get(Integer.parseInt(index));

        FormEntity form = uow.get(FormEntity.class, value.entity().get().identity());

        ValueBuilder<FormValue> builder = vbf.newValueBuilder(FormValue.class);

        builder.prototype().note().set(form.note().get());
        builder.prototype().description().set(form.description().get());
        builder.prototype().form().set(value.entity().get());

        builder.prototype().fields().set(new ListValueBuilder(vbf).addDescribableItems( form.fields()).newList());

        return builder.newInstance();
    }

    public void changeDescription(StringDTO newDescription)
    {
        String identity = getRequest().getAttributes().get("project").toString();
        String index = getRequest().getAttributes().get("index").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        FormsQueries forms = uow.get( FormsQueries.class, identity);

        checkPermission(forms);

        List<ListItemValue> itemValues = forms.applicableFormDefinitionList().items().get();

        ListItemValue value = itemValues.get(Integer.parseInt(index));

        FormEntity form = uow.get(FormEntity.class, value.entity().get().identity());

        form.changeDescription(newDescription.string().get());
    }

    public void changeNote(StringDTO newNote)
    {
        String identity = getRequest().getAttributes().get("project").toString();
        String index = getRequest().getAttributes().get("index").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        FormsQueries forms = uow.get( FormsQueries.class, identity);

        checkPermission(forms);

        List<ListItemValue> itemValues = forms.applicableFormDefinitionList().items().get();

        ListItemValue value = itemValues.get(Integer.parseInt(index));

        FormEntity form = uow.get(FormEntity.class, value.entity().get().identity());

        form.changeNote(newNote.string().get());
    }


    public void deleteOperation()
    {
        String identity = getRequest().getAttributes().get("project").toString();
        String index = getRequest().getAttributes().get("index").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        ProjectEntity project = uow.get( ProjectEntity.class, identity);

        checkPermission(project);

        List<ListItemValue> itemValues = project.applicableFormDefinitionList().items().get();

        ListItemValue value = itemValues.get(Integer.parseInt(index));

        FormEntity form = uow.get(FormEntity.class, value.entity().get().identity());

        project.removeForm(form);
    }
}