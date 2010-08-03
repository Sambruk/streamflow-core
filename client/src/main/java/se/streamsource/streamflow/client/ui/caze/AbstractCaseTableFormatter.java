/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.ui.caze;

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.resource.caze.CaseValue;

import java.util.Comparator;
import java.util.List;

/**
 * JAVADOC
 */
public abstract class AbstractCaseTableFormatter
      implements WritableTableFormat<CaseValue>, AdvancedTableFormat<CaseValue>
{
   protected String[] columnNames;
   protected Class[] columnClasses;

   StringBuilder description = new StringBuilder( );

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

   public Object getColumnValue( CaseValue caseValue, int i )
   {
      switch (i)
      {
         case 0:
         {
            description.setLength( 0 );

            if (caseValue.caseId().get() != null)
               description.append( "#" ).append( caseValue.caseId()).append(" " );

            description.append( caseValue.text().get() );

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
            return caseValue.caseType().get() == null ? null : (caseValue.caseType().get() + (caseValue.resolution().get() == null ? "" : "("+caseValue.resolution().get()+")"));

         case 2:
            return caseValue.creationDate().get();

         case 3:
            return caseValue.status().get();
      }

      return null;
   }

   public boolean isEditable( CaseValue caseValue, int i )
   {
      return false;
   }

   public CaseValue setColumnValue( CaseValue caseValue, Object o, int i )
   {


      return null;
   }
}
