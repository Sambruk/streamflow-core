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
package se.streamsource.streamflow.web.infrastructure.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.spi.query.NamedQueryDescriptor;

public class NamedSolrDescriptor
   implements NamedQueryDescriptor,Serializable
{
   private static final List<String> EMPTY_LIST = new ArrayList<String>();

   private String query;
   private String name;

   public NamedSolrDescriptor( String name, String query )
   {
      if (name == null)
      {
         throw new NullPointerException( "Queries must have a name" );
      }
      if (query == null)
      {
         throw new NullPointerException( "Null queries are not allowed" );
      }

      this.name = name;
      this.query = query;
      EMPTY_LIST.add("query");
   }

   public String name()
   {
      return name;
   }

   public String compose( Map<String, Object> variables,
                          OrderBy[] orderBySegments,
                          Integer firstResult,
                          Integer maxResults )
   {

      return variables.get( "query" ).toString();
   }

   public String language()
   {
      return "solr";
   }

   public List<String> variableNames()
   {
      return EMPTY_LIST;
   }

}

