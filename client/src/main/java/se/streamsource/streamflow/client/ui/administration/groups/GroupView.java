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

import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemCellRenderer;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class GroupView
        extends JPanel
{
    public JList participantList;

    public GroupView(@Service ActionMap am, @Service GroupModel model)
    {
        super(new BorderLayout());

        setActionMap(am);

        participantList = new JList(model);

        participantList.setCellRenderer(new ListItemCellRenderer());

        add(participantList, BorderLayout.CENTER);

        JPanel toolbar = new JPanel();
        toolbar.add(new JButton(am.get("addParticipant")));
        toolbar.add(new JButton(am.get("removeParticipant")));
        add(toolbar, BorderLayout.SOUTH);
    }

    public JList getParticipantList()
    {
        return participantList;
    }
}