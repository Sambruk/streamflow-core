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

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.complete_task_header;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.created_column_header;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.description_column_header;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.task_status_header;

import java.util.Date;

import javax.swing.ImageIcon;

import org.qi4j.api.injection.scope.Uses;

import se.streamsource.streamflow.client.resource.users.workspace.projects.inbox.WorkspaceProjectInboxClientResource;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;

/**
 * JAVADOC
 */
public class WorkspaceProjectInboxModel
        extends TaskTableModel
{
    public WorkspaceProjectInboxModel(@Uses WorkspaceProjectInboxClientResource resource)
    {
        super(resource);
        columnNames = new String[]{text(description_column_header), text(created_column_header), text(task_status_header)};
        columnClasses = new Class[]{String.class, Date.class, ImageIcon.class};
        columnEditable = new boolean[]{false, false, false};
    }

    @Override
    public WorkspaceProjectInboxClientResource getResource()
    {
        return (WorkspaceProjectInboxClientResource) super.getResource();
    }
}