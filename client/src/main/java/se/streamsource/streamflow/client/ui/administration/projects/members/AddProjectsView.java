/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.administration.projects.members;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * JAVADOC
 */
public class AddProjectsView
        extends AbstractTableSelectionView
{
    /*@Service
    OrganizationalUnitAdministrationModel organizationModel;*/

    public AddProjectsView(@Uses AddProjectsModel model,
                           @Structure ValueBuilderFactory vbf)
    {
        super(model, vbf);
    }

    protected String searchLineString()
    {
        return "#Search projects";
    }

    /*protected ListValue findValues(String projectName) throws ResourceException
    {
        return organizationModel.getOrganization().findProjects(projectName);
    }*/
}