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

package se.streamsource.streamflow.client.ui.administration.groups;

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * JAVADOC
 */
public class GroupAdminView
        extends JSplitPane
{
    @Uses 
    ObjectBuilder<GroupView> groupView;

    public GroupAdminView(@Uses GroupsView groupsView,
                          @Uses final GroupsModel groupsModel)
    {
        super();

        setLeftComponent(groupsView);
        setRightComponent(new JPanel());

        setDividerLocation(250);

        final JList list = groupsView.getGroupList();
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                ListItemValue groupValue = (ListItemValue) list.getSelectedValue();
                if (groupValue == null)
                    setRightComponent(new JPanel());
                else
                {
                    setRightComponent(groupView.use(groupsModel.getGroupModel(groupValue.entity().get().identity())).newInstance());
                    try
                    {
                        groupsModel.refresh();
                    } catch (ResourceException e1)
                    {
                        throw new OperationException(AdministrationResources.could_not_refresh_list_of_groups, e1);
                    }
                }
            }
        });
    }

}
