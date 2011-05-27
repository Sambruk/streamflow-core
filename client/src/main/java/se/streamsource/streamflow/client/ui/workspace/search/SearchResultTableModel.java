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

package se.streamsource.streamflow.client.ui.workspace.search;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableModel;
import se.streamsource.streamflow.client.util.EventListSynch;

import java.util.Collections;

/**
 * Model for search results
 */
public class SearchResultTableModel
      extends CasesTableModel
{
   @Structure
   ValueBuilderFactory vbf;

   private String searchString;

   public SearchResultTableModel(@Structure ObjectBuilderFactory obf )
   {
      super( obf );
   }

   public void search( String text )
   {
      searchString = text;

      refresh();
   }

   @Override
   public void refresh()
   {
      if (searchString != null)
      {
         new Task<TableValue, Void>( Application.getInstance(  ))
         {
            @Override
            protected TableValue doInBackground() throws Exception
            {
               return performSearch();
            }

            @Override
            protected void succeeded( TableValue result )
            {
               EventListSynch.synchronize( Collections.<CaseTableValue>emptyList(), eventList );
               EventListSynch.synchronize( caseTableValues( result ), eventList );
            }

            @Override
            protected void failed( Throwable cause )
            {
               throw (RuntimeException) cause;
            }
         }.execute();
      }

      setChanged();
      notifyObservers();
   }

   private TableValue performSearch()
   {
      String translatedQuery = SearchTerms.translate( searchString );

      translatedQuery += addWhereClauseFromFilter();
      
      ValueBuilder<TableQuery> builder = vbf.newValueBuilder( TableQuery.class );
      String query = "select * where " + translatedQuery;

      query += addSortingFromFilter();

      query += " limit 1000";
      builder.prototype().tq().set( query );

      return client.query( "cases", builder.newInstance(), TableValue.class );
   }

    public void clearSearchString() {
        searchString = "";
    }
}