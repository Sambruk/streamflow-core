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

package se.streamsource.streamflow.web.context.surface.endusers;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.table.TableBuilder;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.surface.api.OpenCaseDTO;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.caze.History;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversations;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;

/**
 * Context for open case
 */
public class OpenCaseContext
        implements IndexContext<OpenCaseDTO>
{
   @Structure
   Module module;

   public OpenCaseDTO index()
   {
      ValueBuilderFactory vbf = module.valueBuilderFactory();
      ValueBuilder<OpenCaseDTO> builder = vbf.newValueBuilder(OpenCaseDTO.class);
      CaseEntity aCase = RoleMap.role(CaseEntity.class);
      builder.prototype().description().set(aCase.description().get());
      builder.prototype().creationDate().set(aCase.createdOn().get());
      builder.prototype().caseId().set(aCase.caseId().get());
      builder.prototype().status().set(aCase.status().get().name());

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