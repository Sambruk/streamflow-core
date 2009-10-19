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
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationModel;
import se.streamsource.streamflow.client.ui.workspace.LabelsModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * JAVADOC
 */
public class ProjectAdminView
        extends JSplitPane
{
    @Structure
    ObjectBuilderFactory obf;

    @Uses
    ProjectsModel projectsModel;

    @Uses
    OrganizationalUnitAdministrationModel organizationModel;

    @Service
    DialogService dialogs;
    public JList groupList;
    public DefaultListModel listModel;

    public ProjectAdminView(@Uses final ProjectsView projectsView)
    {
        super();

        setLeftComponent(projectsView);
        setRightComponent(new JPanel());

        setDividerLocation(150);

        final JList list = projectsView.getProjectList();
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    int idx = list.getSelectedIndex();
                    if (idx < list.getModel().getSize() && idx >= 0)
                    {
                        ListItemValue projectValue = (ListItemValue) list.getModel().getElementAt(idx);
                        ProjectMembersModel projectMembersModel = projectsModel.getProjectMembersModel(projectValue.entity().get().identity());
                        LabelsModel projectLabelsModel = projectsModel.getLabelsModel(projectValue.entity().get().identity());
                        FormsModel formsModel = projectsModel.getFormsModel(projectValue.entity().get().identity());
                        projectMembersModel.refresh();
                        projectLabelsModel.refresh();
                        formsModel.refresh();
                        ProjectView view = obf.newObjectBuilder(ProjectView.class).use(
                                projectMembersModel,
                                projectLabelsModel,
                                formsModel,
                                organizationModel).newInstance();
                        setRightComponent(view);
                    } else
                    {
                        setRightComponent(new JPanel());
                    }
                }
            }
        });
    }

}