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

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.resource.organizations.projects.ProjectsClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationModel;
import se.streamsource.streamflow.client.ui.workspace.LabelsModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.roles.StringDTO;

import javax.swing.*;
import java.util.List;

/**
 * List of projects in a OU
 */
public class ProjectsModel
        extends AbstractListModel
{
    @Structure
    ObjectBuilderFactory obf;

    @Structure
    ValueBuilderFactory vbf;

    @Uses
    OrganizationalUnitAdministrationModel organizationModel;

    @Service
    DialogService dialogs;

    ProjectsClientResource projects;
    private List<ListItemValue> list;

    WeakModelMap<String, ProjectMembersModel> projectMembersModels = new WeakModelMap<String, ProjectMembersModel>()
    {
        @Override
        protected ProjectMembersModel newModel(String key)
        {
            return obf.newObjectBuilder(ProjectMembersModel.class).use(projects.project(key).members(), organizationModel).newInstance();
        }
    };

    WeakModelMap<String, LabelsModel> projectLabelsModels = new WeakModelMap<String, LabelsModel>()
    {
        @Override
        protected LabelsModel newModel(String key)
        {
            return obf.newObjectBuilder(LabelsModel.class).use(projects.project(key).labels()).newInstance();
        }
    };

    public ProjectsModel(@Uses ProjectsClientResource projects)
    {
        this.projects = projects;
    }

    public int getSize()
    {
        return list == null ? 0 : list.size();
    }

    public Object getElementAt(int index)
    {
        return list.get(index);
    }

    public void refresh()
    {
        try
        {
            // Get Project list
            list = projects.projects().items().get();

            fireContentsChanged(this, 0, list.size());
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh, e);
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

    public LabelsModel getLabelsModel(String id)
    {
        return projectLabelsModels.get(id);
    }

    public void newProject(String projectName)
    {
        try
        {
            projects.post(new StringRepresentation(projectName));
            refresh();
        } catch (ResourceException e)
        {
            if (Status.CLIENT_ERROR_CONFLICT.equals(e.getStatus()))
            {
                throw new OperationException(AdministrationResources.could_not_create_project_name_already_exists, e);
            }
            throw new OperationException(AdministrationResources.could_not_create_project, e);
        }
    }

    public void describe(int selectedIndex, String newName)
    {
        ValueBuilder<StringDTO> builder = vbf.newValueBuilder(StringDTO.class);
        builder.prototype().string().set(newName);

        try
        {
            projects.project(list.get(selectedIndex).entity().get().identity()).describe(builder.newInstance());
        } catch(ResourceException e)
        {
            if (Status.CLIENT_ERROR_CONFLICT.equals(e.getStatus()))
            {
                throw new OperationException(AdministrationResources.could_not_rename_project_name_already_exists,e);
            }
            throw new OperationException(AdministrationResources.could_not_rename_project,e);
        }
        refresh();
    }
}