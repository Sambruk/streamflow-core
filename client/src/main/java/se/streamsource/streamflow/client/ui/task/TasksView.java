/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.injection.scope.Uses;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class TasksView
   extends JPanel
{
   public TasksView(@Uses TaskTableView taskTableView, @Uses TasksDetailView2 detailsView)
   {
      super(new BorderLayout());

      final JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
      splitPane.setOneTouchExpandable( true );
      add( splitPane, BorderLayout.CENTER );

      splitPane.setTopComponent( taskTableView );
      splitPane.setBottomComponent( detailsView );
      splitPane.setResizeWeight( 0.27D );

      splitPane.setDividerLocation( 1D );
   }
}
