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
package se.streamsource.streamflow.client.ui;

import org.joda.time.DateTime;
import se.streamsource.streamflow.util.Dates;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public class DateFormats
{

   private static SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
   private static SimpleDateFormat formatDay = new SimpleDateFormat("d MMM");

   public static String getProgressiveDateTimeValue(Calendar calendar, Locale locale)
   {
      if (calendar == null)
         return "";

      if (Dates.isToday(calendar))
      {
         return formatTime.format(calendar.getTime());
      } else
      {
         if (Dates.isThisYear(calendar))
         {
            return formatDay.format(calendar.getTime());
         } else
         {
            return (new SimpleDateFormat(getBundle(locale).getString(DateFormatsResources.date.toString()))).format(calendar
                  .getTime());
         }
      }
   }

   public static String getProgressiveDateTimeValue(Date date, Locale locale)
   {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      return getProgressiveDateTimeValue(calendar, locale);
   }

   public static String getFullDateTimeValue(Date date, Locale locale)
   {
      return (new SimpleDateFormat(getBundle(locale).getString(DateFormatsResources.full_datetime.toString()))).format(date);
   }

   private static ResourceBundle getBundle(Locale locale)
   {
      return ResourceBundle.getBundle(DateFormatsResources.class.getName(), locale);
   }

   public static String getProgressiveDateTimeValue( DateTime date, Locale locale )
   {
      return getProgressiveDateTimeValue( date.toDate(), locale );
   }
}
