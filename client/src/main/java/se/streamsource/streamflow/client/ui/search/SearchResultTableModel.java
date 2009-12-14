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

package se.streamsource.streamflow.client.ui.search;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.application.error.ErrorResources;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.users.search.SearchClientResource;
import se.streamsource.streamflow.client.ui.task.TaskTableModel;
import se.streamsource.streamflow.domain.task.TaskStates;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.organization.search.SearchTaskDTO;
import se.streamsource.streamflow.resource.task.TaskDTO;

import javax.swing.ImageIcon;
import java.util.Date;
import java.util.List;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;

/**
 * JAVADOC
 */
public class SearchResultTableModel
      extends TaskTableModel
{
   public SearchResultTableModel( @Uses SearchClientResource resource )
   {
      super( resource );
      columnNames = new String[]{text( title_column_header ), text( project_column_header ), text( assigned_to_header ), text( created_column_header ), ""};
      columnClasses = new Class[]{String.class, String.class, String.class, Date.class, ImageIcon.class};
      columnEditable = new boolean[]{false, false, false, false, false};
   }

   @Override
   protected SearchClientResource getResource()
   {
      return (SearchClientResource) super.getResource();
   }

   public void search( String text )
   {

      try
      {
         getResource().search( text );
      } catch (ResourceException e)
      {
         throw new OperationException( ErrorResources.search_string_malformed, e );
      }
      refresh();
   }

   @Override
   public void markAsUnread( int idx ) throws ResourceException
   {
      // Ignore
   }

   @Override
   public void markAsRead( int idx )
   {
      // Ignore
   }

   public int getColumnCount()
   {
      return 5;
   }


   @Override
   public Object getValueAt( int rowIndex, int column )
   {
      SearchTaskDTO task = (SearchTaskDTO) tasks.get( rowIndex );

      if (task == null)
         return null;

      switch (column)
      {
         case 0:
         {
            StringBuilder desc = new StringBuilder( task.description().get() );
            List<ListItemValue> labels = task.labels().get().items().get();
            if (labels.size() > 0)
            {
               desc.append( " (" );
               String comma = "";
               for (ListItemValue label : labels)
               {
                  desc.append( comma + label.description().get() );
                  comma = ",";
               }
               desc.append( ")" );
            }
            return desc.toString();
         }
         case 1:
            return task.project().get();
         case 2:
            return task.assignedTo().get();
         case 3:
            return task.creationDate().get();
         case 4:
            return task.status().get();
         case IS_READ:
            return task.isRead().get();
         case IS_DROPPED:
            return task.status().get().equals( TaskStates.DROPPED );
      }

      return null;
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