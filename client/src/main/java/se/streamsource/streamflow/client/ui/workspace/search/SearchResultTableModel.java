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

import java.util.Collections;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.ui.workspace.table.PerspectiveModel;
import se.streamsource.streamflow.client.ui.workspace.table.CasesTableModel;
import se.streamsource.streamflow.client.ui.workspace.table.SortBy;
import se.streamsource.streamflow.client.util.EventListSynch;

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
      
      if (!perspectiveModel.getCreatedOn().isEmpty())
      {
            translatedQuery += " createdOn:" + perspectiveModel.getCreatedOn();
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



}