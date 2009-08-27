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

package se.streamsource.streamflow.web.resource.organizations.search;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.resource.organization.search.SearchTaskDTO;
import se.streamsource.streamflow.resource.organization.search.SearchTaskListDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.label.Labelable;
import se.streamsource.streamflow.web.domain.task.TaskEntity;
import se.streamsource.streamflow.web.resource.users.workspace.AbstractTaskListServerResource;

/**
 * JAVADOC
 */
public class SearchTasksServerResource
    extends AbstractTaskListServerResource
{
    public SearchTaskListDTO search(StringDTO query)
    {
        UnitOfWork uow = uowf.currentUnitOfWork();

        if (query.string().get().length() > 0)
        {
            QueryBuilder<TaskEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder(TaskEntity.class);
            String[] searches = query.string().get().split(" ");
            for (int i = 0; i < searches.length; i++)
            {
                String search = searches[i];

                if (search.startsWith("label:"))
                {
                    search = search.substring("label:".length());
                    queryBuilder.where(eq(QueryExpressions.oneOf(templateFor(Labelable.LabelableState.class).labels()).description(), search));
                } else
                {
                    queryBuilder.where(or(
                            eq(templateFor(TaskEntity.class).taskId(), search),
                            matches(templateFor(TaskEntity.class).description(), search),
                            matches(templateFor(TaskEntity.class).note(), search)));
                }
            }

            Query<TaskEntity> tasks = queryBuilder.newQuery(uow);
           return buildTaskList(tasks, SearchTaskDTO.class, SearchTaskListDTO.class);
        } else
        {
            return vbf.newValue(SearchTaskListDTO.class);
        }
    }

}
