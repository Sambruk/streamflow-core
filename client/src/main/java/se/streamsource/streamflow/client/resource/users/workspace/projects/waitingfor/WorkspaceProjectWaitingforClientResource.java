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

package se.streamsource.streamflow.client.resource.users.workspace.projects.waitingfor;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.users.workspace.TaskListClientResource;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.task.TasksQuery;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskListDTO;

import java.util.List;

/**
 * JAVADOC
 */
public class WorkspaceProjectWaitingforClientResource
      extends TaskListClientResource<WorkspaceProjectWaitingforTaskClientResource>
{
   public WorkspaceProjectWaitingforClientResource( @Uses Context context, @Uses Reference reference )
   {
      super( context, reference, WorkspaceProjectWaitingforTaskClientResource.class );
   }

   public List<TaskDTO> tasks( TasksQuery query ) throws ResourceException
   {
      return query( "tasks", query, WaitingForTaskListDTO.class ).<WaitingForTaskListDTO>buildWith().prototype().tasks().get();
   }
}
