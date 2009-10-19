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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.task.TaskClientResource;

/**
 * Model for task details.
 */
public class TaskModel
        implements Refreshable
{
    @Uses
    private TaskClientResource resource;

    @Uses
    private TaskCommentsModel comments;

    @Uses
    private TaskGeneralModel general;

    @Uses
    private TaskContactsModel contacts;

    public TaskClientResource resource()
    {
        return resource;
    }

    public TaskCommentsModel comments()
    {
        return comments;
    }

    public TaskGeneralModel general()
    {
        return general;
    }

    public TaskContactsModel contacts()
    {
        return contacts;
    }

    public void refresh()
    {
        general.refresh();
        comments.refresh();
        contacts.refresh();
    }
}