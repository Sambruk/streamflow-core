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

package se.streamsource.streamflow.client.ui.administration;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;

/**
 * Management of selected entities
 */
public class SelectionListModel
   extends LinkValueListModel
{
   private String possible;

   public SelectionListModel( String possible )
   {
      this("index", possible);
   }

   public SelectionListModel(String refresh, String possible)
   {
      super(refresh);

      this.possible = possible;
   }

   public EventList<LinkValue> getPossible()
   {
      BasicEventList<LinkValue> possibleLinks = new BasicEventList<LinkValue>();
      possibleLinks.addAll( client.query( possible, LinksValue.class ).links().get() );
      return possibleLinks;
   }

   public void add( Iterable<? extends LinkValue> selected)
   {
      for (LinkValue linkValue : selected)
      {
         client.postLink( linkValue );
      }
   }
}