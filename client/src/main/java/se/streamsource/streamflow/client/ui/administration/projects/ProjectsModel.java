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
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.organizations.projects.ProjectsClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.AbstractListModel;

/**
 * List of projects in a OU
 */
public class ProjectsModel
        extends AbstractListModel
{
    @Structure
    ObjectBuilderFactory obf;

    ProjectsClientResource projects;
    private ListValue list;

    WeakModelMap<String, ProjectMembersModel> projectMembersModels = new WeakModelMap<String, ProjectMembersModel>()
    {
        @Override
        protected ProjectMembersModel newModel(String key)
        {
            return obf.newObjectBuilder(ProjectMembersModel.class).use(projects.project(key)).newInstance();
        }
    };

    public ProjectsModel(@Uses ProjectsClientResource projects)
    {
        this.projects = projects;
    }

    public int getSize()
    {
        return list == null ? 0 : list.items().get().size();
    }

    public Object getElementAt(int index)
    {
        return list.items().get().get(index);
    }

    private void refresh()
    {
        try
        {
            list = projects.projects();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh_list_of_projects, e);
        }
    }

    public void removeProject(String id)
    {
        try
        {
            projects.project(id).delete();
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_remove_project, e);
        }
    }

    public ProjectMembersModel getProjectMembersModel(String id)
    {
        return projectMembersModels.get(id);
    }

    public void newProject(String projectName)
    {
        try
        {
            projects.post(new StringRepresentation(projectName));
            refresh();
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_create_project, e);
        }

    }
}