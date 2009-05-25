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

package se.streamsource.streamflow.client.ui.administration.projects;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.organizations.projects.ProjectClientResource;
import se.streamsource.streamflow.client.resource.organizations.projects.ProjectsClientResource;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.roles.DescriptionValue;

import javax.swing.DefaultListModel;

/**
 * List of projects in a OU
 */
public class ProjectsModel
        extends DefaultListModel
{
    @Structure
    ValueBuilderFactory vbf;

    ProjectsClientResource projects;

    public void setProjects(ProjectsClientResource projects) throws ResourceException
    {
        this.projects = projects;

        refresh();
    }

    private void refresh() throws ResourceException
    {
        clear();
        for (ListItemValue value : projects.projects().items().get())
        {
            addElement(value);
        }
    }

    public void removeProject(String id) throws ResourceException
    {
        projects.project(id).delete();
        refresh();
    }

    public ProjectClientResource getProjectResource(String id)
    {
        return projects.project(id);
    }

    public void newProject(String text) throws ResourceException
    {
        ValueBuilder<DescriptionValue> builder = vbf.newValueBuilder(DescriptionValue.class);
        builder.prototype().description().set(text);
        projects.newProject(builder.newInstance());

        refresh();
    }
}