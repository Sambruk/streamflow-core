/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.context.surface.endusers;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.query.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.util.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.table.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.surface.api.*;
import se.streamsource.streamflow.util.*;
import se.streamsource.streamflow.web.domain.entity.caze.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;
import se.streamsource.streamflow.web.domain.structure.caze.*;
import se.streamsource.streamflow.web.domain.structure.conversation.*;

import java.util.*;

/**
 * Context for closed case
 */
public class ClosedCaseContext
        implements IndexContext<ClosedCaseDTO>
{
   @Structure
   Module module;

   public ClosedCaseDTO index()
   {
      ValueBuilderFactory vbf = module.valueBuilderFactory();
      ValueBuilder<ClosedCaseDTO> builder = vbf.newValueBuilder(ClosedCaseDTO.class);
      CaseEntity aCase = RoleMap.role(CaseEntity.class);
      builder.prototype().description().set(aCase.description().get());
      builder.prototype().creationDate().set(aCase.createdOn().get());
      Date closedDate = aCase.getHistoryMessage("closed").createdOn().get();
      builder.prototype().closeDate().set(closedDate);

      if (aCase.resolution().get() != null)
         builder.prototype().resolution().set(aCase.resolution().get().getDescription());
      
      builder.prototype().caseId().set(aCase.caseId().get());

      Owner owner = aCase.owner().get();
      builder.prototype().project().set(((Describable) owner).getDescription());

      return builder.newInstance();
   }

   public TableValue history(TableQuery tq)
   {
      String select = "*".equals(tq.select().trim()) ? "sender,message,created" : tq.select();
      String[] columns = select.split("[, ]");

      Messages.Data messages = (Messages.Data) RoleMap.role(History.class).getHistory();
      Query<Message> query = module.queryBuilderFactory().newQueryBuilder(Message.class).newQuery(messages.messages());
      if (tq.offset() != null)
         query.firstResult(Integer.parseInt(tq.offset()));
      if (tq.limit() != null)
         query.maxResults(Integer.parseInt(tq.limit()));

      TableBuilder table = new TableBuilder(module.valueBuilderFactory());
      for (String column : columns)
      {
         if (column.equals("created"))
            table.column("created","Created", "date");
         else
            table.column(column, Strings.humanReadable(column), "string");
      }

      for (Message message : query)
      {
         Message.Data data = (Message.Data) message;
         table.row();
         for (String column : columns)
         {
            if (column.equals("sender"))
               table.cell(data.sender().get().getDescription(), data.sender().get().getDescription());
            else if (column.equals("message"))
               table.cell(data.body().get(), data.body().get());
            else if (column.equals("created"))
               table.cell(data.createdOn().get(), DateFunctions.toUtcString(data.createdOn().get()));
         }
         table.endRow();
      }

      return table.newTable();
   }
}
