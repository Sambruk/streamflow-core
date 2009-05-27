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

package se.streamsource.streamflow.client.ui.administration.projects.members;

import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.ui.administration.projects.ProjectModel;
import se.streamsource.streamflow.client.ui.administration.groups.GroupsModel;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * JAVADOC
 */
public class AddGroupsView
        extends JPanel
{
    private AddGroupsModel addGroupsModel;

    public AddGroupsView(@Service GroupsModel groupsModel,
                         @Uses AddGroupsModel addGroupsModel)
    {
        super(new BorderLayout());
        this.addGroupsModel = addGroupsModel;

        add(new JLabel("#Select Groups to be added"), BorderLayout.NORTH);
        try
        {
            ListValue groups = groupsModel.getGroups().groups();
            addGroupsModel.setGroups(groups);
        } catch (ResourceException e)
        {
            e.printStackTrace();
        }
        JXTable memberGroupsTable = new JXTable(addGroupsModel);
        memberGroupsTable.getColumn(0).setMaxWidth(40);
        memberGroupsTable.getColumn(0).setResizable(false);
        JScrollPane groupsScrollPane = new JScrollPane(memberGroupsTable);
        add(groupsScrollPane);
    }


    public AddGroupsModel getModel()
    {
        return addGroupsModel;
    }
}