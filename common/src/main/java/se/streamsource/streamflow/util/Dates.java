package se.streamsource.streamflow.util;

import java.util.Calendar;
import java.util.Date;

public class Dates
{
   public static boolean isSameDay(Date firstDate, Date secondDate)
   {
      if (firstDate == null || secondDate == null)
      {
         throw new IllegalArgumentException("The Dates can't be null");
      }
      Calendar firstCalendar = Calendar.getInstance();
      firstCalendar.setTime(firstDate);
      Calendar secondCalendar = Calendar.getInstance();
      secondCalendar.setTime(secondDate);
      return isSameDay(firstCalendar, secondCalendar);
   }

   public static boolean isSameDay(Calendar firstCalendar, Calendar secondCalendar)
   {
      if (firstCalendar == null || secondCalendar == null)
      {
         throw new IllegalArgumentException("The Calendars can't be null");
      }
      return (isSameYear(firstCalendar, secondCalendar) && firstCalendar
            .get(Calendar.DAY_OF_YEAR) == secondCalendar.get(Calendar.DAY_OF_YEAR));
   }

   public static boolean isToday(Date date)
   {
      return isSameDay(date, Calendar.getInstance().getTime());
   }

   public static boolean isToday(Calendar calendar)
   {
      return isSameDay(calendar, Calendar.getInstance());
   }
   
   public static boolean isSameYear(Date firstDate, Date secondDate)
   {
      if (firstDate == null || secondDate == null)
      {
         throw new IllegalArgumentException("The Dates can't be null");
      }
      Calendar firstCalendar = Calendar.getInstance();
      firstCalendar.setTime(firstDate);
      Calendar second = Calendar.getInstance();
      second.setTime(secondDate);
      return isSameYear(firstCalendar, second);
   }
   
   public static boolean isSameYear(Calendar firstCalendar, Calendar secondCalendar)
   {
      if (firstCalendar == null || secondCalendar == null)
      {
         throw new IllegalArgumentException("The Calendars can't be null");
      }
      return (firstCalendar.get(Calendar.ERA) == secondCalendar.get(Calendar.ERA)
            && firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR));
   }
   
   public static boolean isThisYear(Date date)
   {
      return isSameYear(date, Calendar.getInstance().getTime());
   }
   
   public static boolean isThisYear(Calendar calendar)
   {
      return isSameYear(calendar, Calendar.getInstance());
   }
}
