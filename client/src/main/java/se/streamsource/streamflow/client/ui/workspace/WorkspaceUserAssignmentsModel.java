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

package se.streamsource.streamflow.client.ui.workspace;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.resource.users.workspace.user.assignments.WorkspaceUserAssignmentsClientResource;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;

import javax.swing.ImageIcon;
import java.util.Date;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;

/**
 * JAVADOC
 */
public class WorkspaceUserAssignmentsModel
      extends TaskTableModel
{
   public WorkspaceUserAssignmentsModel( @Uses WorkspaceUserAssignmentsClientResource resource )
   {
      super( resource );
      columnNames = new String[]{text( title_column_header ), text( created_column_header ), text( task_status_header )};
      columnClasses = new Class[]{String.class, Date.class, ImageIcon.class};
      columnEditable = new boolean[]{false, false, false};
   }

   @Override
   public WorkspaceUserAssignmentsClientResource getResource()
   {
      return (WorkspaceUserAssignmentsClientResource) super.getResource();
   }
}
