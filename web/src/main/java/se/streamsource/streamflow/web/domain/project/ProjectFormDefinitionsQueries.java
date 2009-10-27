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

package se.streamsource.streamflow.web.domain.project;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.web.domain.form.FormDefinitionsQueries;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(ProjectFormDefinitionsQueries.FormDefinitionsQueriesMixin.class)
public interface ProjectFormDefinitionsQueries
{
    ListValue applicableFormDefinitionList();

    ListValue nonApplicableFormDefinitionList();

    class FormDefinitionsQueriesMixin
        implements ProjectFormDefinitionsQueries
    {
        @This
        ProjectFormDefinitions.ProjectFormDefinitionsState state;

        @This
        ProjectOrganization.ProjectOrganizationState organizationState;

        @Structure
        ValueBuilderFactory vbf;

        public ListValue applicableFormDefinitionList()
        {
            return new ListValueBuilder(vbf).addDescribableItems( state.formDefinitions() ).newList();
        }

        public ListValue nonApplicableFormDefinitionList()
        {
            FormDefinitionsQueries queries = (FormDefinitionsQueries) organizationState.organizationalUnit().get();

            List<ListItemValue> allForms = queries.formDefinitionList().items().get();
            List<ListItemValue> applicable = (new ListValueBuilder(vbf).addDescribableItems( state.formDefinitions() ).newList()).items().get();

            List<ListItemValue> nonApplicable = new ArrayList<ListItemValue>();

            for (ListItemValue form : allForms)
            {
                if (!applicable.contains(form))
                {
                    nonApplicable.add(form);
                }
            }

            ValueBuilder<ListValue> listBuilder =  vbf.newValueBuilder(ListValue.class);
            listBuilder.prototype().items().set(nonApplicable);

            return listBuilder.newInstance();
        }
    }
}