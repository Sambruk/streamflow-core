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

package se.streamsource.streamflow.client.ui.workspace.search;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.ui.workspace.caze.table.CasesModel;
import se.streamsource.streamflow.client.ui.workspace.caze.table.CasesTableModel;

/**
 * JAVADOC
 */
public class SearchResultTableModel
      extends CasesTableModel
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   CasesModel casesModel;

   private String searchString;

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
         final LinksValue newRoot = performSearch();
         EventListSynch.synchronize( newRoot.links().get(), eventList );
      }
   }

   private LinksValue performSearch()
   {
      String translatedQuery = SearchTerms.translate( searchString );

      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( translatedQuery );

      return client.query( "search", builder.newInstance(), LinksValue.class );
   }

}