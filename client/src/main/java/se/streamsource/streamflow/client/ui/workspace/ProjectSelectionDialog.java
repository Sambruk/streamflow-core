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

package se.streamsource.streamflow.client.ui.workspace;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSelectionView;
import se.streamsource.streamflow.client.ui.administration.projects.members.TableSingleSelectionModel;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Select project.
 */
public class ProjectSelectionDialog
        extends JPanel
{
    Dimension dialogSize = new Dimension(600,300);
    private TableSelectionView addProjectsView;
    public ListItemValue selected;

    public ProjectSelectionDialog(final @Uses WorkspaceProjectNode project,
                               @Service ApplicationContext context,
                               @Structure ObjectBuilderFactory obf)
    {
        super(new BorderLayout());

        setName("#Search project");
        setActionMap(context.getActionMap(this));

        TableSingleSelectionModel projectsModel = obf.newObject(TableSingleSelectionModel.class);
        this.addProjectsView = obf.newObjectBuilder(TableSelectionView.class).use(projectsModel, "#Search projects").newInstance();

        addProjectsView.getSearchInputField().addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent keyEvent)
            {
                try
                {
                    ListValue value = project.findProjects(addProjectsView.searchText());
                    addProjectsView.getModel().setModel(value);
                } catch (ResourceException e)
                {
                    e.printStackTrace();
                }
            }
        });


        setPreferredSize(dialogSize);
        add(addProjectsView, BorderLayout.NORTH);
    }

    public EntityReference getSelected()
    {
        return selected == null ? null : selected.entity().get();
    }

    @Action
    public void execute()
    {
        selected = ((TableSingleSelectionModel) addProjectsView.getModel()).getSelected();

        WindowUtils.findWindow(this).dispose();
    }

    @Action
    public void close()
    {
        WindowUtils.findWindow(this).dispose();
    }
}