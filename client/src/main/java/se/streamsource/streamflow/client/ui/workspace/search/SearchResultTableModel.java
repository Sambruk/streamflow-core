/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.ui.workspace.search;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableModel;
import se.streamsource.streamflow.client.ui.workspace.table.Period;
import se.streamsource.streamflow.client.ui.workspace.table.PerspectiveModel;
import se.streamsource.streamflow.client.ui.workspace.table.SortBy;
import se.streamsource.streamflow.client.util.EventListSynch;

import java.util.Collections;
import java.util.Date;

/**
 * Model for search results
 */
public class SearchResultTableModel
      extends CasesTableModel
{
   public SearchResultTableModel(@Uses PerspectiveModel perspecitveModel)
   {
      super(perspecitveModel);
   }

   @Structure
   ValueBuilderFactory vbf;

   private String searchString;

   public void search( String text )
   {
      searchString = text;

      refresh();
   }

   @Override
   public void refresh()
   {
      if (searchString != null)
      {
         new Task<TableValue, Void>( Application.getInstance(  ))
         {
            @Override
            protected TableValue doInBackground() throws Exception
            {
               return performSearch();
            }

            @Override
            protected void succeeded( TableValue result )
            {
               EventListSynch.synchronize( Collections.<CaseTableValue>emptyList(), eventList );
               EventListSynch.synchronize( caseTableValues( result ), eventList );
            }

            @Override
            protected void failed( Throwable cause )
            {
               throw (RuntimeException) cause;
            }
         }.execute();
      }
   }

   private TableValue performSearch()
   {
      String translatedQuery = SearchTerms.translate( searchString );

      if (!perspectiveModel.getSelectedStatuses().isEmpty())
      {
         translatedQuery += " status:";
         String comma = "";
         for (String status : perspectiveModel.getSelectedStatuses())
         {
            translatedQuery += comma + status;
            comma = ",";
         }
      }
      
      if (!perspectiveModel.getSelectedCaseTypes().isEmpty())
      {
         translatedQuery += " caseType:\"";
         String comma = "";
         for (String caseType : perspectiveModel.getSelectedCaseTypes())
         {
            translatedQuery += comma + caseType;
            comma = ",";
         }
         translatedQuery +=  "\"";
      }
      
      if (!perspectiveModel.getSelectedLabels().isEmpty())
      {
         translatedQuery += " label:\"";
         String comma = "";
         for (String label : perspectiveModel.getSelectedLabels())
         {
            translatedQuery += comma + label;
            comma = ",";
         }
         translatedQuery +=  "\"";
      }

      if (!perspectiveModel.getSelectedAssignees().isEmpty())
      {
         translatedQuery += " assignedTo:\"";
         String comma = "";
         for (String assignee : perspectiveModel.getSelectedAssignees())
         {
            translatedQuery += comma + assignee;
            comma = ",";
         }
         translatedQuery +=  "\"";
      }

      if (!perspectiveModel.getSelectedProjects().isEmpty())
      {
         translatedQuery += " project:\"";
         String comma = "";
         for (String project : perspectiveModel.getSelectedProjects())
         {
            translatedQuery += comma + project;
            comma = ",";
         }
         translatedQuery +=  "\"";
      }

      if (!perspectiveModel.getSelectedCreatedBy().isEmpty())
      {
         translatedQuery += " createdBy:\"";
         String comma = "";
         for (String createdBy : perspectiveModel.getSelectedCreatedBy())
         {
            translatedQuery += comma + createdBy;
            comma = ",";
         }
         translatedQuery +=  "\"";
      }
      
      if ( !Period.none.equals( perspectiveModel.getCreatedPeriod() ) )
      {
         Date fromDate =  perspectiveModel.getCreatedOn() != null ? perspectiveModel.getCreatedOn() : new Date();
         int direction = perspectiveModel.getCreatedOn() != null ? 1 : -1;

         translatedQuery += " createdOn:" + getSearchPeriod( fromDate, direction, perspectiveModel.getCreatedPeriod().name() );

      }

      if( !Period.none.equals( perspectiveModel.getDueOnPeriod() ))
      {
         Date fromDate =  perspectiveModel.getDueOn() != null ? perspectiveModel.getDueOn() : new Date();
         int direction = perspectiveModel.getDueOn() != null ? 1 : -1;

         translatedQuery += " dueOn:" + getSearchPeriod( fromDate, direction, perspectiveModel.getDueOnPeriod().name() );
      }
      
      ValueBuilder<TableQuery> builder = vbf.newValueBuilder( TableQuery.class );
      String query = "select * where " + translatedQuery;
      if (perspectiveModel.getSortBy() != SortBy.none)
      {
         query += " order by " + perspectiveModel.getSortBy().name() + " " + perspectiveModel.getSortOrder().name();
      }
      query += " limit 1000";
      builder.prototype().tq().set( query );

      return client.query( "cases", builder.newInstance(), TableValue.class );
   }

   private String getSearchPeriod( Date fromDate, int direction, String periodName )
   {
      DateMidnight from = new DateMidnight( fromDate );
      DateMidnight to = null;
      DateTimeFormatter format = DateTimeFormat.forPattern( "yyyyMMdd" );


      switch (Period.valueOf( periodName ))
      {
         case one_day:
            return format.print( from);

         case three_days:
            to = ( direction == 1) ? from.plusDays( 3 ) : from.minusDays( 3 );
            break;

         case one_week:
            to = ( direction == 1 ) ? from.plusWeeks( 1 ) : from.minusWeeks( 1 );
            break;

         case two_weeks:
            to = ( direction == 1 ) ? from.plusWeeks( 2 ) : from.minusWeeks( 2 );
            break;

         case one_month:
            to = ( direction == 1 ) ? from.plusMonths( 1 ) : from.minusMonths( 1 );
            break;

         case six_months:
            to = ( direction == 1 ) ? from.plusMonths( 6 ) : from.minusMonths( 6 );
            break;

         case one_year:
            to = ( direction == 1 ) ? from.plusYears( 1 ) : from.minusYears( 1 );
            break;

      }
      return (direction == 1)
                  ? format.print( from ) + "-" + format.print( to )
                  : format.print( to ) + "-" + format.print( from );

   }

}