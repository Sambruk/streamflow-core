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

package se.streamsource.streamflow.web.resource.surface.endusers;

import org.qi4j.api.util.Function;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.value.table.TableBuilderFactory;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.web.context.surface.endusers.OpenCaseContext;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;

/**
 * Resource for an open case
 */
public class OpenCaseResource
        extends CommandQueryResource
{
   public OpenCaseResource()
   {
      super(OpenCaseContext.class);
   }

   public TableValue history() throws Throwable
   {
      Iterable<Message> history = (Iterable<Message>) invoke();

      TableQuery query = (TableQuery) getArguments()[0];

      return new TableBuilderFactory(module.valueBuilderFactory()).
              column("created", "Created", TableValue.DATETIME, new Function<Message.Data, Object>()
              {
                 public Object map(Message.Data data)
                 {
                    return data.createdOn().get();
                 }
              }).
              column("message", "Message", TableValue.STRING, new Function<Message.Data, Object>()
              {
                 public Object map(Message.Data data)
                 {
                    return data.body().get();
                 }
              }).
              column("sender", "Sender", TableValue.STRING, new Function<Message.Data, Object>()
              {
                 public Object map(Message.Data data)
                 {
                    return data.sender().get().getDescription();
                 }
              }).
              newInstance(query).rows(history).orderBy().paging().newTable();
   }
}
