/**
 *
 * Copyright 2009-2011 Streamsource AB
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
package se.streamsource.streamflow.client.ui.workspace.cases.caselog;

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryDTO;

import javax.swing.Icon;
import java.util.Comparator;

import static se.streamsource.streamflow.client.Icons.*;
import static se.streamsource.streamflow.client.util.i18n.*;

public class CaseLogTableFormatter
      implements WritableTableFormat<Object>, AdvancedTableFormat<Object>
   {
      protected Class[] columnClasses;

      public CaseLogTableFormatter()
      {
         columnClasses = new Class[]{
            Icon.class,
            CaseLogEntryDTO.class
         };
      }

      public Class getColumnClass( int i )
      {
         return columnClasses[i];
      }

      public Comparator getColumnComparator( int i )
      {
         return null;
      }

      public int getColumnCount()
      {
         return columnClasses.length;
      }

      public String getColumnName( int i )
      {
         return "";
      }


      public Object getColumnValue( Object value, int i )
      {
         CaseLogEntryDTO caseLogEntry = (CaseLogEntryDTO)value;

         switch(i){
            case 0:
               return caseLogEntry.myPagesVisibility().get() ? icon( published, ICON_16 )
                     : icon( not_published, ICON_16 );

            case 1:
               return caseLogEntry;

            case 2:
               return caseLogEntry.id().get();

         }
         return null;
      }

      public boolean isEditable( Object obj, int i )
      {
         return i == 0;
      }

      public Object setColumnValue( Object obj, Object obj1, int i )
      {
         return obj;
      }
   }
