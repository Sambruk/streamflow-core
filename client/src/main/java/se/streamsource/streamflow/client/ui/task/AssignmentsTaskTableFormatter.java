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

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import se.streamsource.streamflow.domain.interaction.gtd.States;

import java.util.Date;

/**
 * JAVADOC
 */
public class AssignmentsTaskTableFormatter
      extends AbstractTaskTableFormatter
{
   public AssignmentsTaskTableFormatter()
   {
      columnNames = new String[]{
            text( title_column_header ),
            text( tasktype_column_header ),
            text( created_column_header ),
            text( task_status_header )};
      columnClasses = new Class[] {
            String.class,
            String.class,
            Date.class,
            States.class
            };
   }
}