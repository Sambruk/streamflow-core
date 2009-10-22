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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.task.TaskClientResource;
import se.streamsource.streamflow.client.resource.task.TasksClientResource;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

/**
 * Model that keeps track of all task models
 */
public class TasksModel
    implements EventListener
{
    @Uses
    TasksClientResource tasksResource;

    @Structure
    ObjectBuilderFactory obf;

    WeakModelMap<String, TaskModel> models = new WeakModelMap<String, TaskModel>()
    {
        protected TaskModel newModel(String key)
        {
            TaskClientResource taskResource = tasksResource.task( key );
            return obf.newObjectBuilder( TaskModel.class).
                    use( taskResource,
                            taskResource.general(),
                            taskResource.comments(),
                            taskResource.contacts(),
                            taskResource.forms()).newInstance();
        }
    };


    public TaskModel task(String id)
    {
        return models.get( id );
    }

    public void notifyEvent( DomainEvent event )
    {
        for (TaskModel model : models)
        {
            model.notifyEvent(event);
        }
    }
}
