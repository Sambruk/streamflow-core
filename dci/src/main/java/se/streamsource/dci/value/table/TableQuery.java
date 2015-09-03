/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.dci.value.table;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

import se.streamsource.dci.value.table.gdq.GdQuery;
import se.streamsource.dci.value.table.gdq.GdQueryParser;
import se.streamsource.dci.value.table.gdq.OrderByElement;

import java.util.List;

/**
 * Query value for Google Data queries.
 */
@Mixins(TableQuery.Mixin.class)
public interface TableQuery
        extends ValueComposite
{
   Property<String> tq();

   public List<String> select();

   public String where();

   public List<OrderByElement> orderBy();

   public Integer limit();

   public Integer offset();

   public String options();

   abstract class Mixin
           implements TableQuery
   {
      private GdQuery gdQuery;

      public List<String> select()
      {
         return getGdQuery().select;
      }

      public String where()
      {
         return getGdQuery().where;
      }

      public List<OrderByElement> orderBy()
      {
         return getGdQuery().orderBy;
      }

      public Integer limit()
      {
         return getGdQuery().limit;
      }

      public Integer offset()
      {
         return getGdQuery().offset;
      }

      public String options()
      {
         return getGdQuery().options;
      }

      private GdQuery getGdQuery()
      {
         if (gdQuery == null) {
            gdQuery = GdQueryParser.parse(tq().get());
         }

         return gdQuery;
      }
   }

}
