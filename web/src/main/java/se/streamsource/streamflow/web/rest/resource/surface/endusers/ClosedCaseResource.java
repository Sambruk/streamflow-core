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
package se.streamsource.streamflow.web.rest.resource.surface.endusers;

import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.util.Function;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.dci.value.table.TableBuilder;
import se.streamsource.dci.value.table.TableBuilderFactory;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryDTO;
import se.streamsource.streamflow.web.context.surface.endusers.ClosedCaseContext;
import se.streamsource.streamflow.web.context.surface.endusers.OpenCaseContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;
import se.streamsource.streamflow.web.rest.resource.surface.endusers.submittedforms.MyPagesSubmittedFormsResource;

/**
 * Resource for a closed case
 */
public class ClosedCaseResource
        extends CommandQueryResource
{
   public ClosedCaseResource()
   {
      super(ClosedCaseContext.class);
   }

   public TableValue caselog(TableQuery tq) throws Throwable
   {
      Iterable<CaseLogEntryDTO> caselog = context( ClosedCaseContext.class ).caselog( );

      TableBuilderFactory tableBuilderFactory = new TableBuilderFactory( module.valueBuilderFactory() );

      tableBuilderFactory.column( "created", "Created", TableValue.DATETIME, new Function<CaseLogEntryDTO, Object>()
      {
         public Object map(CaseLogEntryDTO entry)
         {
            return entry.creationDate().get();
         }
      } ).column( "message", "Message", TableValue.STRING, new Function<CaseLogEntryDTO, Object>()
      {
         public Object map(CaseLogEntryDTO entry)
         {
            return entry.text().get();
         }
      } ).column( "sender", "Sender", TableValue.STRING, new Function<CaseLogEntryDTO, Object>()
      {
         public Object map(CaseLogEntryDTO entry)
         {
            return entry.creator().get();
         }
      } );

      TableBuilder tableBuilder = tableBuilderFactory.newInstance( tq );

      TableValue table = tableBuilder.rows( caselog ).orderBy().paging().newTable();
      return table;
   }
   
   @SubResource
   public void submittedforms( )
   {
      subResource( MyPagesSubmittedFormsResource.class );
   }
}
