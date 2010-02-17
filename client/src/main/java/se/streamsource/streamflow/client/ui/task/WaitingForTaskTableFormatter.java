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

import se.streamsource.streamflow.domain.interaction.gtd.States;
import se.streamsource.streamflow.resource.task.TaskValue;

import java.util.Date;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;

/**
 * JAVADOC
 */
public class WaitingForTaskTableFormatter
   extends AbstractTaskTableFormatter
{
   public WaitingForTaskTableFormatter()
   {
      columnNames = new String[]{
            text( title_column_header ),
            text( tasktype_column_header ),
            text( delegated_to_header ),
            text( assigned_to_header ),
            text( delegated_on_header ),
            text( task_status_header )};
      columnClasses = new Class[] {
            String.class,
            String.class,
            String.class,
            String.class,
            Date.class,
            States.class
            };
   }

   public Object getColumnValue( TaskValue taskValue, int i )
   {
      switch (i)
      {
         case 0:
         case 1:
         {
            return super.getColumnValue( taskValue, i );
         }

         case 2:
            return taskValue.delegatedTo().get();

         case 3:
            return taskValue.assignedTo().get();

         case 4:
            return taskValue.delegatedOn().get();

         case 5:
            return super.getColumnValue( taskValue, 3 );
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