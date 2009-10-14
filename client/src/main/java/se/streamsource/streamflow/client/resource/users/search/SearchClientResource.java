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

package se.streamsource.streamflow.client.resource.users.search;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.users.workspace.TaskListClientResource;
import se.streamsource.streamflow.resource.organization.search.SearchTaskDTO;
import se.streamsource.streamflow.resource.organization.search.SearchTaskListDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;

import java.util.List;

/**
 * JAVADOC
 */
public class SearchClientResource
        extends TaskListClientResource<SearchTaskClientResource>
{
    public List<SearchTaskDTO> tasks;

    public SearchClientResource(@Uses Context context, @Uses Reference reference, @Structure ValueBuilderFactory vbf)
    {
        super(context, reference, SearchTaskClientResource.class);

        tasks = vbf.newValue(SearchTaskListDTO.class).tasks().get();
    }

    public List<SearchTaskDTO> tasks(TasksQuery query) throws ResourceException
    {
        return tasks;
    }

    public void search(String search) throws ResourceException
    {
        ValueBuilder<StringDTO> builder = vbf.newValueBuilder(StringDTO.class);
        builder.prototype().string().set(search);
        tasks = query("search", builder.newInstance(), SearchTaskListDTO.class).<SearchTaskListDTO>buildWith().prototype().tasks().get();
    }
}
