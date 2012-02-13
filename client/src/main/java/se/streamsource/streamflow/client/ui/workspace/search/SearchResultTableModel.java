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
package se.streamsource.streamflow.client.ui.workspace.search;

import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableModel;

/**
 * Model for search results
 */
public class SearchResultTableModel
        extends CasesTableModel
{
   private String searchString;

   public SearchResultTableModel(@Structure Module module)
   {
      super(module);
   }

   public void search(String text)
   {
      searchString = text;

      refresh();
   }

   @Override
   public void refresh()
   {
      if (searchString != null)
      {
         TableValue result = performSearch();

         eventList.getReadWriteLock().writeLock().lock();
         try
         {
            if (eventList instanceof TransactionList)
               ((TransactionList) eventList).beginEvent();

               eventList.clear();
               eventList.addAll( caseTableValues(result) );

            if (eventList instanceof TransactionList)
               ((TransactionList) eventList).commitEvent();
         } finally
         {
            eventList.getReadWriteLock().writeLock().unlock();
         }

         setChanged();
         notifyObservers();
      }
   }

   private TableValue performSearch()
   {
      String translatedQuery = SearchTerms.translate(searchString);

      translatedQuery += addWhereClauseFromFilter();

      ValueBuilder<TableQuery> builder = module.valueBuilderFactory().newValueBuilder(TableQuery.class);
      String query = "select * where " + translatedQuery;

      query += addSortingFromFilter();

      query += " limit 1000";
      builder.prototype().tq().set(query);

      return client.query("cases", TableValue.class, builder.newInstance());
   }

    public void clearSearchString() {
        searchString = "";
    }
}