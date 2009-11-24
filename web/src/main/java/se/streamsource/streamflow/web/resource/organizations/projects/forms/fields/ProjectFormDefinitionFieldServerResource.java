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

package se.streamsource.streamflow.web.resource.organizations.projects.forms.fields;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.web.domain.form.FieldEntity;
import se.streamsource.streamflow.web.domain.form.FormEntity;
import se.streamsource.streamflow.web.domain.form.FormsQueries;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /organizations/{organization}/projects/{project}/forms/{index}/fields/{fieldIndex}
 */
public class ProjectFormDefinitionFieldServerResource
        extends CommandQueryServerResource
{
    public FieldDefinitionValue field()
    {
        String identity = getRequest().getAttributes().get("project").toString();

        String index = getRequest().getAttributes().get("index").toString();

        String fieldIndex = getRequest().getAttributes().get("fieldIndex").toString();

        UnitOfWork uow = uowf.currentUnitOfWork();

        FormsQueries forms = uow.get( FormsQueries.class, identity);

        checkPermission(forms);

        List<ListItemValue> itemValues = forms.applicableFormDefinitionList().items().get();

        ListItemValue value = itemValues.get(Integer.parseInt(index));

        FormEntity form = uow.get(FormEntity.class, value.entity().get().identity());

        FieldEntity field = (FieldEntity) form.fields().get(Integer.parseInt(fieldIndex));

        ValueBuilder<FieldDefinitionValue> builder = vbf.newValueBuilder(FieldDefinitionValue.class);

        builder.prototype().note().set(field.note().get());
        builder.prototype().description().set(field.description().get());
        builder.prototype().valueDefinition().set(field.valueDefinition().get().getDescription());

        return builder.newInstance();
    }
}