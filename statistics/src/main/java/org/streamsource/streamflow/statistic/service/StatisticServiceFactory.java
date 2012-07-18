/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package org.streamsource.streamflow.statistic.service;

import org.streamsource.streamflow.statistic.dto.SearchCriteria;

/**
 * Factory for StatisticService by periodicity.
 */
public class StatisticServiceFactory
{
      
   public static StatisticService getInstance( SearchCriteria criteria )
   {
      StatisticService result = null;

      switch(SearchCriteria.SearchPeriodicity.valueOf( criteria.getPeriodicity().name() ))
      {
         case weekly:
            result = new WeeklyStatisticService( criteria );
            break;
         
         case monthly:
            result = new MonthlyStatisticService( criteria );
            break;
         
         case yearly:
            result = new YearlyStatisticService( criteria );
            break;
      }
      return result;
   }
}
