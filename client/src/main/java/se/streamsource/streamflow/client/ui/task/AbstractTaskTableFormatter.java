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

package se.streamsource.streamflow.client.ui.task;

import ca.odell.glazedlists.gui.WritableTableFormat;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.resource.task.TaskDTO;

import java.util.List;

/**
 * JAVADOC
 */
public abstract class AbstractTaskTableFormatter
      implements WritableTableFormat<TaskDTO>
{
   protected String[] columnNames;
   
   public int getColumnCount()
   {
      return columnNames.length;
   }

   public String getColumnName( int i )
   {
      return columnNames[i];
   }

   public Object getColumnValue( TaskDTO taskDTO, int i )
   {
      switch (i)
      {
         case 0:
         {
            StringBuilder desc = new StringBuilder( taskDTO.description().get() );
            List<ListItemValue> labels = taskDTO.labels().get().items().get();
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
            return taskDTO.taskType().get();

         case 2:
            return taskDTO.creationDate().get();

         case 3:
            return taskDTO.status().get();
      }

      return null;
   }

   public boolean isEditable( TaskDTO taskDTO, int i )
   {
      return false;
   }

   public TaskDTO setColumnValue( TaskDTO taskDTO, Object o, int i )
   {


      return null;
   }
}
