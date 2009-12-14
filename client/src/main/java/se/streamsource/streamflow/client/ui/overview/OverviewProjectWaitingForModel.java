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

package se.streamsource.streamflow.client.ui.overview;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.users.overview.projects.waitingfor.OverviewProjectWaitingForClientResource;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.resource.task.TaskDTO;
import se.streamsource.streamflow.resource.waitingfor.WaitingForTaskDTO;

import javax.swing.ImageIcon;
import java.util.Date;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;

/**
 * JAVADOC
 */
public class OverviewProjectWaitingForModel
      extends TaskTableModel
{
   public OverviewProjectWaitingForModel( @Uses OverviewProjectWaitingForClientResource resource )
   {
      super( resource );
      columnNames = new String[]{text( title_column_header ), text( delegated_to_header ), text( assigned_to_header ), text( delegated_on_header ), ""};
      columnClasses = new Class[]{String.class, String.class, String.class, Date.class, ImageIcon.class};
      columnEditable = new boolean[]{false, false, false, false};
   }

   @Override
   protected OverviewProjectWaitingForClientResource getResource()
   {
      return (OverviewProjectWaitingForClientResource) super.getResource();
   }

   @Override
   public int getColumnCount()
   {
      return 5;
   }

   @Override
   public Object getValueAt( int rowIndex, int column )
   {
      WaitingForTaskDTO task = (WaitingForTaskDTO) tasks.get( rowIndex );
      switch (column)
      {
         case 1:
            return task.delegatedTo().get();
         case 2:
            return task.assignedTo().get();
         case 3:
            return task.delegatedOn().get();
         case 4:
            return task.status().get();
      }

      return super.getValueAt( rowIndex, column );
   }

   @Override
   public void setValueAt( Object aValue, int rowIndex, int column )
   {
      try
      {
         switch (column)
         {
            case 0:
            {
               String description = (String) aValue;
               TaskDTO taskValue = (TaskDTO) tasks.get( rowIndex );
               if (!description.equals( taskValue.description().get() ))
               {
                  taskValue.description().set( description );
                  fireTableCellUpdated( rowIndex, column );
               }
               break;
            }
            case 4:
            {
               Boolean completed = (Boolean) aValue;
               if (completed)
               {

                  TaskDTO taskValue = (TaskDTO) tasks.get( rowIndex );
                  if (taskValue.status().get() == TaskStates.ACTIVE)
                  {
                     EntityReference task = taskValue.task().get();
                     getResource().task( task.identity() ).complete();

                     taskValue.status().set( TaskStates.COMPLETED );
                     fireTableCellUpdated( rowIndex, column );
                  }
               }
               break;
            }
         }
      } catch (ResourceException e)
      {
         // TODO Better error handling
         e.printStackTrace();
      }

      return; // Skip if don't know what is going on
   }
}