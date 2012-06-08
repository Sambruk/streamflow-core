/**
 *
 * Copyright 2009-2012 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.workspace.table;

import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.priority.CasePriorityValue;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.util.Strings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;
import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * JAVADOC
 */
public class CasesTableFormatter
      implements AdvancedTableFormat<Object>
{
   protected String[] columnNames;
   protected Class[] columnClasses;

   StringBuilder description = new StringBuilder();


   public CasesTableFormatter()
   {
      columnNames = new String[]{
            text( title_column_header ),
            text( info_column_header ),
            text( casetype_column_header ),
            text( assignee_column_header ),
            text( created_column_header ),
            text( created_by_column_header ),
            text( duedate_column_header ),
            text( project_column_header ),
            text( case_priority_header ),
            text( case_status_header )};
      columnClasses = new Class[]{
            String.class,
            ArrayList.class,
            String.class,
            String.class,
            Date.class,
            String.class,
            Date.class,
            String.class,
            CasePriorityValue.class,
            CaseStates.class
      };
   }

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

   public Object getColumnValue( Object value, int i )
   {
      if (value instanceof SeparatorList.Separator)
      {
         SeparatorList.Separator separator = (SeparatorList.Separator) value;
         return separator;
      } else
      {
         CaseTableValue caseValue = (CaseTableValue) value;
         switch (i)
         {
            case 0:
            {
               description.setLength( 0 );

               if (!Strings.empty( caseValue.caseId().get() ))
                  description.append( "#" ).append( caseValue.caseId() ).append( " " );

               description.append( caseValue.description().get() );

               List<LinkValue> labels = caseValue.labels().get().links().get();
               if (labels.size() > 0)
               {
                  description.append( " (" );
                  String comma = "";
                  for (LinkValue label : labels)
                  {
                     description.append( comma ).append( label.text().get() );
                     comma = ",";
                  }
                  description.append( ")" );
               }
               return description.toString();
            }

            case 1:
               ArrayList<String> icons = new ArrayList<String>();

               icons.add( caseValue.hasContacts().get() ? Icons.projects.toString() : "empty" );
               icons.add( caseValue.hasConversations().get() ? Icons.conversations.toString() : "empty" );
               icons.add( caseValue.hasSubmittedForms().get() ? Icons.forms.toString() : "empty" );
               icons.add( caseValue.hasAttachments().get() ? Icons.attachments.toString() : "empty" );

               return icons;
            case 2:
               return caseValue.caseType().get() == null ? null : (caseValue.caseType().get()
                     + (Strings.empty( caseValue.resolution().get() ) ? "" : "(" + caseValue.resolution().get() + ")"));

            case 3:
               return caseValue.assignedTo().get();

            case 4:
               return caseValue.creationDate().get();

            case 5:
               return caseValue.createdBy().get();

            case 6:
               return caseValue.dueOn().get();

            case 7:
               return caseValue.owner().get();

            case 8:
               return caseValue.priority().get();

            case 9:
               return caseValue.status().get();

           case 10:
               return caseValue.href().get();
         }

         return null;
      }
   }
}
