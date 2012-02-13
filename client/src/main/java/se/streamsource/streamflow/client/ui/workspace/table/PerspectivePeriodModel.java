/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.workspace.table;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Uses;

import java.util.Date;

/**
 * The backing model for PerspectivePeriodModel
 */
public class PerspectivePeriodModel
{
   private Date date;
   private Period period;

   public void PerspectiveModel(@Uses @Optional Date date, @Uses Period period)
   {
      this.date = date;
      this.period = period;
   }

   public Date getDate()
   {
      return date;
   }

   public void setDate(Date date)
   {
      this.date = date;
   }

   public Period getPeriod()
   {
      return period;
   }

   public void setPeriod(Period period)
   {
      this.period = period;
   }

   public String getSearchValue(String datePattern, String separator)
   {
      if (!period.equals(Period.none))
      {
         if (date == null)
         {
            return getSearchPeriod(new Date(), -1, getPeriod().name(), datePattern, separator);
         } else
         {
            return getSearchPeriod(date, 1, getPeriod().name(), datePattern, separator);
         }
      }
      return "";
   }

   private String getSearchPeriod(Date fromDate, int direction, String periodName, String datePattern, String separator)
   {
      DateMidnight from = new DateMidnight(fromDate);
      DateMidnight to = null;
      DateTimeFormatter format = DateTimeFormat.forPattern(datePattern);


      switch (Period.valueOf(periodName))
      {
         case one_day:
            return format.print(from);

         case three_days:
            to = (direction == 1) ? from.plusDays(3) : from.minusDays(3);
            break;

         case one_week:
            to = (direction == 1) ? from.plusWeeks(1) : from.minusWeeks(1);
            break;

         case two_weeks:
            to = (direction == 1) ? from.plusWeeks(2) : from.minusWeeks(2);
            break;

         case one_month:
            to = (direction == 1) ? from.plusMonths(1) : from.minusMonths(1);
            break;

         case six_months:
            to = (direction == 1) ? from.plusMonths(6) : from.minusMonths(6);
            break;

         case one_year:
            to = (direction == 1) ? from.plusYears(1) : from.minusYears(1);
            break;

      }
      return (direction == 1)
              ? format.print(from) + separator + format.print(to)
              : format.print(to) + separator + format.print(from);

   }
}
