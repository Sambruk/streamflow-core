/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.overview;

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import se.streamsource.streamflow.client.ui.task.TaskTableView;
import se.streamsource.streamflow.client.ui.workspace.ProjectSelectionDialog;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * JAVADOC
 */
public class OverviewProjectAssignmentsView
      extends TaskTableView
{
   @Uses
   protected ObjectBuilder<ProjectSelectionDialog> projectSelectionDialog;

   protected void buildPopupMenu( JPopupMenu popup )
   {
   }

   @Override
   protected void buildToolbar( JPanel toolbar )
   {
   }
}