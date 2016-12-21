/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.dci.value.table;

import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * JAVADOC
 */
public class QueryBuilder<T extends QueryBuilder>
{
   protected ValueBuilder<TableQuery> queryBuilder;

   private StringBuilder select;

   public QueryBuilder( ValueBuilderFactory vbf )
   {
      queryBuilder = vbf.newValueBuilder( TableQuery.class );
   }

   public T select(String... columns)
   {
      if (select == null)
         select = new StringBuilder(  );

      for (String column : columns)
      {
         if (select.length() > 0)
            select.append( ',' );
         select.append( column );
      }

      return (T) this;
   }

   public TableQuery newQuery()
   {
      String query = "";

      if (select != null)
         query+= "select "+select.toString();

      queryBuilder.prototype().tq().set( query );

      return queryBuilder.newInstance();
   }
}
