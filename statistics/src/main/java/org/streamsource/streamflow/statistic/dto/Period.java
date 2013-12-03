/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package org.streamsource.streamflow.statistic.dto;

/**
 * Created by IntelliJ IDEA. User: arvidhuss Date: 2/20/12 Time: 3:26 PM To
 * change this template use File | Settings | File Templates.
 */
public class Period implements Comparable<Period>
{
   private String period;
   private String periodLabel;
   private Integer count = new Integer( 0 );

   public Period(String period)
   {
      this.period = period;
      this.periodLabel = period;
   }

   public String getPeriod()
   {
      return period;
   }

   public void setPeriod(String period)
   {
      this.period = period;
   }

   public String getPeriodLabel()
   {
      return periodLabel;
   }

   public void setPeriodLabel(String periodLabel)
   {
      this.periodLabel = periodLabel;
   }

   public Integer getCount()
   {
      return count;
   }

   public void setCount(Integer count)
   {
      this.count = count;
   }

   public int compareTo(Period o)
   {
      return this.period.compareTo( ((Period) o).period );
   }
}
