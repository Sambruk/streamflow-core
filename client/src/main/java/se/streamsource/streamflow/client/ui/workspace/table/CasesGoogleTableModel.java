/*
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

package se.streamsource.streamflow.client.ui.workspace.table;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.table.*;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;

/**
 * Base class for all models that list cases
 */
public class CasesGoogleTableModel
      implements Refreshable
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   protected CommandQueryClient client;

   protected EventList<RowValue> rows = new BasicEventList<RowValue>();
   protected EventList<ColumnValue> columns = new BasicEventList<ColumnValue>();
   private TableQuery query;

   public void setQuery(TableQuery query)
   {
      this.query = query;
   }

   public EventList<ColumnValue> getColumns()
   {
      return columns;
   }

   public EventList<RowValue> getRows()
   {
      return rows;
   }

   public void refresh()
   {
      if (query != null)
      {
         TableResponseValue tableResponse = client.query("inbox", query, TableResponseValue.class);
         TableValue table = tableResponse.table().get();
         EventListSynch.synchronize( table.cols().get(), columns );
         EventListSynch.synchronize( table.rows().get(), rows );
      }
   }
}