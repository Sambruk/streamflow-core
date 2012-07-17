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
package org.streamsource.streamflow.statistic.dto;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Search criteria
 */
public class SearchCriteria
{

   public enum SearchPeriodicity {
      weekly,
      monthly,
      yearly
   }
   private DateTime fromDate;
   private DateTime toDate;
   private SearchPeriodicity periodicity;

   private DateTimeFormatter dateOutFormatter = DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm:ss" );
   private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern( "yyyy-MM-dd" );
   private DateTimeFormatter monthFormatter = DateTimeFormat.forPattern( "yyyy-MM" );
   private DateTimeFormatter weekFormatter = DateTimeFormat.forPattern( "yyyy-ww" );
   private DateTimeFormatter yearFormatter = DateTimeFormat.forPattern( "yyyy" );
   
   private String[] periods;
   
   public SearchCriteria(String fromDate, String toDate, String periodicity) {
      if (fromDate != null && !"".equals( fromDate )) {
         this.fromDate = dateFormatter.parseDateTime( fromDate );
      } else {
         this.fromDate = new DateTime(  ).withDayOfYear( 1 );
      }
      if (toDate != null && !"".equals( toDate )){
         this.toDate = dateFormatter.parseDateTime( toDate ).withTime( 23,59,0,0 );
      } else {
         this.toDate = new DateTime().withTime( 23,59,0,0 );
      }
      if (periodicity == null || "".equals( periodicity ) ){
         this.periodicity = SearchPeriodicity.monthly;
      } else {
         this.periodicity = SearchPeriodicity.valueOf(periodicity);
      }
      
      periods = calculatePeriods();
   }

   public DateTime getFromDate()
   {
      return fromDate;
   }

   public DateTime getToDate()
   {
      return toDate.toDateTime();
   }

   public SearchPeriodicity getPeriodicity()
   {
      return periodicity;
   }

   public String getFormattedFromDate()
   {
      return fromDate.toString( dateFormatter );
   }

   public String getFormattedToDateTime()
   {
      return toDate.toString( dateOutFormatter );
   }

   public String getFormattedToDate()
   {
      return toDate.toString( dateFormatter );
   }

   public String[] calculatePeriods()
   {
      String[] result = null;
      if(periodicity.equals( SearchPeriodicity.weekly ))
      {
         result = calculatePeriods( weekFormatter );
      }
      else if( periodicity.equals(  SearchPeriodicity.monthly ))
      {
         result = calculatePeriods( monthFormatter );
      }
      else
      {
         result = calculatePeriods( yearFormatter );
      }

      return result;
   }

   public String[] calculatePeriods( DateTimeFormatter formatter )
   {
      List<String> periods = new ArrayList<String>(  );
      // first period
      periods.add( fromDate.toString( formatter ) );

      DateTime tmpDateTime = null;
      int count = 1;
      while( toDate.isAfter( tmpDateTime = getIncreaseInterval( count ) ) )
      {
         periods.add( tmpDateTime.toString( formatter ) );
         count++;
      }
      if( !periods.contains( toDate.toString( formatter )))
         periods.add( toDate.toString( formatter ) );

      Collections.sort( periods );

      return periods.toArray( new String[ periods.size() ] );
   }

   private DateTime getIncreaseInterval( int count )
   {
      DateTime result = null;
      switch( SearchPeriodicity.valueOf( periodicity.name() ) )
      {
         case weekly:
            result = fromDate.plusWeeks( count );
            break;
         case monthly:
            result = fromDate.plusMonths( count );
            break;
         case yearly:
            result = fromDate.plusYears( count );
            break;
      }
      return result;
   }

   public String[] getPeriods()
   {
      return periods;
   }
}
