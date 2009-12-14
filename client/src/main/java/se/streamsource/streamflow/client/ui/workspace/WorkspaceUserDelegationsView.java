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

package se.streamsource.streamflow.client.ui.workspace;

import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.ui.task.TaskTableView;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * JAVADOC
 */
public class WorkspaceUserDelegationsView
      extends TaskTableView
{

   protected void buildPopupMenu( JPopupMenu popup )
   {
      ActionMap am = getActionMap();
      Action markTasksAsUnread = am.get( "markTasksAsUnread" );
      popup.add( markTasksAsUnread );
      Action markTasksAsRead = am.get( "markTasksAsRead" );
      popup.add( markTasksAsRead );
      taskTable.getSelectionModel().addListSelectionListener( new TaskSelectionActionEnabler( 3, taskTable, markTasksAsRead, markTasksAsUnread ) );
   }

   @Override
   protected void buildToolbar( JPanel toolbar )
   {
      Action acceptAction = addToolbarButton( toolbar, "completeTasks" );
      Action assignAction = addToolbarButton( toolbar, "assignTasksToMe" );
      Action delegateTasksFromInbox = addToolbarButton( toolbar, "reject" );
      addToolbarButton( toolbar, "refresh" );
      taskTable.getSelectionModel().addListSelectionListener( new TaskSelectionActionEnabler( 3, taskTable, assignAction, delegateTasksFromInbox, acceptAction ) );
   }


   @org.jdesktop.application.Action
   public void reject() throws ResourceException
   {
      WorkspaceUserDelegationsModel delegationsModel = (WorkspaceUserDelegationsModel) model;
      for (int row : getReverseSelectedTasks())
      {
         delegationsModel.reject( row );
      }
      model.refresh();
   }
}
