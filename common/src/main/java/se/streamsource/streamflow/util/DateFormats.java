package se.streamsource.streamflow.util;

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
}
