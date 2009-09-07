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

package se.streamsource.streamflow.client.ui.search;

import org.restlet.resource.ResourceException;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.resource.organizations.search.SearchClientResource;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.created_column_header;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.description_column_header;

import java.util.Date;

/**
 * JAVADOC
 */
public class SearchResultTableModel
        extends TaskTableModel
{
    public SearchResultTableModel()
    {
        columnNames = new String[]{"", text(description_column_header), text(created_column_header)};
        columnClasses = new Class[]{Boolean.class, String.class, Date.class};
        columnEditable = new boolean[]{false, false, false};
    }

    @Override
    public SearchClientResource getResource()
    {
        return (SearchClientResource) super.getResource();
    }

    public void search(String text) throws ResourceException
    {
        getResource().search(text);
        refresh();
    }

    @Override
    public void markAsUnread(int idx) throws ResourceException
    {
        // Ignore
    }

    @Override
    public void markAsRead(int idx) throws ResourceException
    {
        // Ignore
    }
}