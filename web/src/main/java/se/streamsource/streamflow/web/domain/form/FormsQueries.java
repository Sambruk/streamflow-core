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

package se.streamsource.streamflow.web.domain.form;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.web.domain.project.ProjectOrganization;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(FormsQueries.Mixin.class)
public interface FormsQueries
{
    ListValue applicableFormDefinitionList();

    ListValue nonApplicableFormDefinitionList();

    class Mixin
        implements FormsQueries
    {
        @This
        Forms.Data state;

        @This
        ProjectOrganization.Data organizationState;

        @Structure
        ValueBuilderFactory vbf;

        public ListValue applicableFormDefinitionList()
        {
            return new ListValueBuilder(vbf).addDescribableItems( state.forms() ).newList();
        }

        public ListValue nonApplicableFormDefinitionList()
        {
            FormQueries queries = (FormQueries) organizationState.organizationalUnit().get();

            List<ListItemValue> allForms = queries.getForms().items().get();
            List<ListItemValue> applicable = (new ListValueBuilder(vbf).addDescribableItems( state.forms() ).newList()).items().get();

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