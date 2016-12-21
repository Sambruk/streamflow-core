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
package org.streamsource.streamflow.statistic.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: arvidhuss Date: 2/20/12 Time: 12:53 PM To
 * change this template use File | Settings | File Templates.
 */
public class CaseCount implements Comparable<CaseCount>
{
   protected String name;
   protected Integer total = new Integer( 0 );
   protected Map<String, Period> values;

   public CaseCount(String name, String[] periods)
   {
      this.name = name;
      this.values = new HashMap<String, Period>();

      for (int i = 0; i < periods.length; i++)
      {
         this.values.put( periods[i], new Period( periods[i] ) );
      }
   }

   public String getName()
   {
      return name;
   }

   public Integer getTotal()
   {
      return total;
   }

   public void addCount(String period, Integer count)
   {
      values.get( period ).setCount( count );
      total += count;
   }

   public List<Period> getValues()
   {
      ArrayList<Period> periods = new ArrayList<Period>( values.values() );
      Collections.sort( periods );

      return periods;
   }

   public int compareTo(CaseCount o)
   {
      return ((CaseCount) o).total - this.total;
   }
}
