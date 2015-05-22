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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query value for Google Data queries.
 */
@Mixins(TableQuery.Mixin.class)
public interface TableQuery
        extends ValueComposite
{
   Property<String> tq();

   public String select();

   public String where();

   public String groupBy();

   public String pivot();

   public String orderBy();

   public String limit();

   public String offset();

   public String label();

   public String options();

   abstract class Mixin
           implements TableQuery
   {
      private static Collection<String> keywords = Arrays.asList("select", "where", "group by", "pivot", "order by", "limit", "offset", "label", "options");

      private Map<String, String> parts;

      public String select()
      {
         return getParts().get("select");
      }

      public String where()
      {
         return getParts().get("where");
      }

      public String groupBy()
      {
         return getParts().get("group by");
      }

      public String pivot()
      {
         return getParts().get("pivot");
      }

      public String orderBy()
      {
         return getParts().get("order by");
      }

      public String limit()
      {
         return getParts().get("limit");
      }

      public String offset()
      {
         return getParts().get("offset");
      }

      public String label()
      {
         return getParts().get("label");
      }

      public String options()
      {
         return getParts().get("options");
      }

      private Map<String, String> getParts()
      {
         if (parts == null)
         {
            parts = new HashMap<String, String>();

            String value = tq().get();
            List<String> values = Arrays.asList(value.split(" "));
            Collections.reverse(values);
            String currentPhrase = "";
            for (String str : values)
            {
               currentPhrase = str + currentPhrase;
               boolean found = false;
               for (String keyword : keywords)
               {
                  if (currentPhrase.startsWith(keyword + " "))
                  {
                     found = true;
                     parts.put(keyword, currentPhrase.substring(keyword.length() + 1));
                     currentPhrase = "";
                     break;
                  }
               }

               if (!found)
                  currentPhrase = " " + currentPhrase;
            }
         }

         return parts;
      }
   }
}
