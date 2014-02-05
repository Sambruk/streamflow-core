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
package org.streamsource.streamflow.statistic.service;

import org.streamsource.streamflow.statistic.dto.SearchCriteria;
import org.streamsource.streamflow.statistic.web.Dao;

/**
 * Statistic service for week period.
 */
public class WeeklyStatisticService
   extends StatisticService
{
   
   public WeeklyStatisticService( SearchCriteria criteria )
   {
      super( criteria );
   }

   public String getPeriodFunction( String column )
   {
      if(Dao.getDbVendor().equalsIgnoreCase( "mssql" ))
      {
         return "CONVERT( VARCHAR(5), [" + column + "], 120 ) + CONVERT( VARCHAR(2),{ fn WEEK([" + column + "]) })";
      } else
      {
         return "date_format( " + column + ", '%Y-%v' )";
      }
   }

   public String getGroupOrOrderByClause( String column, String alias )
   {
      if(Dao.getDbVendor().equalsIgnoreCase( "mssql" ))
      {
         return getPeriodFunction( column );
      } else
      {
         return alias;
      }
   }

}
