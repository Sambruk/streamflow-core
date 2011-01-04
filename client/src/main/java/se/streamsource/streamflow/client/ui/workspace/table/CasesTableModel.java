/*
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

package se.streamsource.streamflow.client.ui.workspace.table;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.table.*;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.domain.interaction.gtd.CaseStates;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Base class for all models that list cases
 */
public class CasesTableModel
      implements Refreshable
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   protected CommandQueryClient client;

   protected EventList<CaseTableValue> eventList = new BasicEventList<CaseTableValue>();

   public EventList<CaseTableValue> getEventList()
   {
      return eventList;
   }

   public void refresh()
   {
      ValueBuilder<TableQuery> builder = vbf.newValueBuilder( TableQuery.class );
      builder.prototype().tq().set( "select *" );
      TableQuery query = builder.newInstance();

      TableValue table = client.query( "cases", query, TableValue.class );
      List<CaseTableValue> caseTableValues = caseTableValues( table );
      EventListSynch.synchronize( caseTableValues, eventList );
   }

   protected List<CaseTableValue> caseTableValues( TableValue table )
   {
      List<CaseTableValue> caseTableValues = new ArrayList<CaseTableValue>(  );
      for(RowValue row : table.rows().get())
      {
         ValueBuilder<CaseTableValue> caseBuilder = vbf.newValueBuilder( CaseTableValue.class );
         CaseTableValue prototype = caseBuilder.prototype();
         List<CellValue> cells = row.c().get();
         for (int i = 0; i < table.cols().get().size(); i++)
         {
            ColumnValue columnValue = table.cols().get().get( i );
            CellValue cell = cells.get( i );
            if (columnValue.id().get().equals("assigned"))
               prototype.assignedTo().set( cell.f().get() );
            else if (columnValue.id().get().equals("caseid"))
               prototype.caseId().set( cell.f().get() );
            else if (columnValue.id().get().equals("casetype"))
               prototype.caseType().set(cell.f().get());
            else if (columnValue.id().get().equals("creator"))
               prototype.createdBy().set(cell.f().get());
            else if (columnValue.id().get().equals("created"))
               prototype.creationDate().set((Date) cell.v().get());
            else if (columnValue.id().get().equals("due"))
               prototype.dueOn().set((Date) cell.v().get());
            else if (columnValue.id().get().equals("description"))
               prototype.description().set(cell.f().get());
            else if (columnValue.id().get().equals("hasattachments"))
               prototype.hasAttachments().set((Boolean) cell.v().get());
            else if (columnValue.id().get().equals("hascontacts"))
               prototype.hasContacts().set((Boolean) cell.v().get());
            else if (columnValue.id().get().equals("hasconversations"))
               prototype.hasConversations().set((Boolean) cell.v().get());
            else if (columnValue.id().get().equals("hassubmittedforms"))
               prototype.hasSubmittedForms().set((Boolean) cell.v().get());
            else if (columnValue.id().get().equals("labels"))
            {
               String json = cell.v().get().toString();
               prototype.labels().set(vbf.newValueFromJSON( LinksValue.class, json ));
            }
            else if (columnValue.id().get().equals("owner"))
               prototype.owner().set(cell.f().get());
            else if (columnValue.id().get().equals("parent") && cell.v().get() != null)
               prototype.parentCase().set(vbf.newValueFromJSON( LinkValue.class, cell.v().get().toString()));
            else if (columnValue.id().get().equals("resolution"))
               prototype.resolution().set(cell.f().get());
            else if (columnValue.id().get().equals("status"))
               prototype.status().set( CaseStates.valueOf( cell.v().get().toString() ));
            else if (columnValue.id().get().equals("subcases"))
            {
               prototype.subcases().set( vbf.newValueFromJSON( LinksValue.class, cell.v().get().toString() ) );
            } else if (columnValue.id().get().equals( "href" ))
               prototype.href().set( cell.f().get() );
         }
         caseTableValues.add(caseBuilder.newInstance());
      }
      return caseTableValues;
   }
}