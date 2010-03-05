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

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.resource.task.TaskValue;

import java.util.Comparator;
import java.util.List;

/**
 * JAVADOC
 */
public abstract class AbstractTaskTableFormatter
      implements WritableTableFormat<TaskValue>, AdvancedTableFormat<TaskValue>
{
   protected String[] columnNames;
   protected Class[] columnClasses;

   StringBuilder desc = new StringBuilder( );

   public int getColumnCount()
   {
      return columnNames.length;
   }

   public String getColumnName( int i )
   {
      return columnNames[i];
   }

   public Class getColumnClass( int i )
   {
      return columnClasses[i];
   }

   public Comparator getColumnComparator( int i )
   {
      return null;
   }

   public Object getColumnValue( TaskValue taskValue, int i )
   {
      switch (i)
      {
         case 0:
         {
            desc.setLength( 0 );

            if (taskValue.taskId().get() != null)
               desc.append( "#" ).append( taskValue.taskId()).append(" " );

            desc.append( taskValue.text().get() );

            List<LinkValue> labels = taskValue.labels().get().links().get();
            if (labels.size() > 0)
            {
               desc.append( " (" );
               String comma = "";
               for (LinkValue label : labels)
               {
                  desc.append( comma + label.text().get() );
                  comma = ",";
               }
               desc.append( ")" );
            }
            return desc.toString();
         }

         case 1:
            return taskValue.taskType().get();

         case 2:
            return taskValue.creationDate().get();

         case 3:
            return taskValue.status().get();
      }

      return null;
   }

   public boolean isEditable( TaskValue taskValue, int i )
   {
      return false;
   }

   public TaskValue setColumnValue( TaskValue taskValue, Object o, int i )
   {


      return null;
   }
}
