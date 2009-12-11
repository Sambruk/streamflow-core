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

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsView;
import se.streamsource.streamflow.client.ui.administration.tasktypes.SelectedTaskTypesView;

import javax.swing.JTabbedPane;

/**
 * JAVADOC
 */
public class ProjectView
        extends JTabbedPane
{
    public ProjectView(@Uses ProjectMembersView membersView,
                       @Uses SelectedLabelsView selectedLabelsView,
                       @Uses SelectedTaskTypesView selectedTaskTypesView)
    {
        super();

        addTab("Members", membersView);
        addTab("Labels", selectedLabelsView );
        addTab("Task types", selectedTaskTypesView);
    }
}