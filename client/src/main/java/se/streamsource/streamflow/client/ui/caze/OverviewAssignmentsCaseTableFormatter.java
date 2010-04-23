/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.caze;

import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;
import se.streamsource.streamflow.resource.caze.CaseValue;

import java.util.Date;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import static se.streamsource.streamflow.client.ui.overview.OverviewResources.assigned_to_column_header;
import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;

/**
 * JAVADOC
 */
public class OverviewAssignmentsCaseTableFormatter
   extends AbstractCaseTableFormatter
{
   public OverviewAssignmentsCaseTableFormatter()
   {
      columnNames = new String[]{
         text( title_column_header ),
         text( assigned_to_column_header ),
         text( casetype_column_header ),
         text( created_column_header ),
         text( case_status_header )};
      columnClasses = new Class[] {
            String.class,
            String.class,
            String.class,
            Date.class,
            CaseStates.class
            };
   }

   @Override
   public Object getColumnValue( CaseValue caseValue, int i )
   {
      switch (i)
      {
         case 0:
            return super.getColumnValue( caseValue, i );
         case 1:
            return caseValue.assignedTo().get();
         default:
            return super.getColumnValue( caseValue, i-1 );
      }
   }
}