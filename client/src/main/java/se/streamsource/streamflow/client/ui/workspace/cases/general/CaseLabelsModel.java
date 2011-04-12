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

package se.streamsource.streamflow.client.ui.workspace.cases.general;

import ca.odell.glazedlists.*;
import org.qi4j.api.injection.scope.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.*;

/**
 * Model for the list of currently selected labels of a case
 */
public class CaseLabelsModel
   extends ResourceModel<LinksValue>
   implements TransactionListener
{
   @Uses
   CommandQueryClient client;

   EventList<LinkValue> labels = new TransactionList<LinkValue>( new BasicEventList<LinkValue>() );

   public EventList<LinkValue> getLabels()
   {
      return labels;
   }

   public void refresh()
   {
      super.refresh();
      EventListSynch.synchronize( getIndex().links().get(), labels );
   }

   public EventList<LinkValue> getPossibleLabels()
   {
      BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

      LinksValue listValue = client.query( "possiblelabels",
            LinksValue.class );
      list.addAll( listValue.links().get() );

      return list;
   }

   public void addLabel( LinkValue addLabel )
   {
      client.postLink( addLabel );
   }

   public void removeLabel( LinkValue removeLabel )
   {
      client.getClient( removeLabel ).delete();
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.withNames("addedLabel", "removedLabel" ), transactions ))
         refresh();
   }
}
