/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.administration.projectsettings;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.LinkValueListModel;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * JAVADOC
 */
public class RecipientsModel
   extends LinkValueListModel
{
   public EventList<LinkValue> getPossibleRecipients()
   {
      return EventListSynch.synchronize(client.query("possiblerecipients", LinksValue.class).links().get(), new BasicEventList<LinkValue>());
   }

   public void addRecipient(LinkValue link)
   {
      client.postLink( link );
   }

   public void removeRecipient(LinkValue link)
   {
      client.postLink( link );
   }

}