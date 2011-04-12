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

package se.streamsource.streamflow.client.ui.administration.surface;

import ca.odell.glazedlists.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import org.restlet.data.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.dci.value.table.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;

import static org.qi4j.api.specification.Specifications.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * TODO
 */
public class EmailAccessPointsModel
        implements Refreshable, TransactionListener
{
   @Uses
   protected CommandQueryClient client;

   @Structure
   protected ValueBuilderFactory vbf;

   protected EventList<RowValue> rowValues = new TransactionList<RowValue>(new BasicEventList<RowValue>());

   public EmailAccessPointsModel()
   {
   }

   public void refresh()
   {
      EventListSynch.synchronize(client.query("index", TableValue.class).rows().get(), rowValues);
   }

   public EventList<RowValue> getRows()
   {
      return rowValues;
   }

   public void remove(int index)
   {
      client.getClient(index + "/").delete();
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      // Refresh if either the owner of the list has changed, or if any of the entities in the list has changed
      if (matches(or(onEntities(client.getReference().getParentRef().getLastSegment()), onEntities(client.getReference().getLastSegment())), transactions))
         refresh();
   }

   public EventList<LinkValue> possibleAccessPoints()
   {
      EventList<LinkValue> eventList = new BasicEventList<LinkValue>();
      EventListSynch.synchronize(client.query("possibleaccesspoints", LinksValue.class).links().get(), eventList);
      return eventList;
   }

   public void create(String email, LinkValue createEmailAccessPoint)
   {
      Form form = new Form();
      form.set("email", email);
      client.getClient(createEmailAccessPoint).postCommand("", form.getWebRepresentation());
   }
}
