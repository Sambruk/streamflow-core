/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration.labels;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.util.DefinitionListModel;

/**
 * Management of labels
 */
public class LabelsModel
   extends DefinitionListModel
{
   public LabelsModel( )
   {
      super( "create" );

       relationModelMapping("resource", LabelModel.class );
   }

   public EventList<LinkValue> getPossibleMoveTo(LinkValue selected)
   {
      BasicEventList<LinkValue> possibleLinks = new BasicEventList<LinkValue>();
      possibleLinks.addAll( client.getClient(selected).query( "possiblemoveto", LinksValue.class ).links().get() );
      return possibleLinks;
   }

   public void moveForm( LinkValue selected, LinkValue selectedLink )
   {
      client.getClient( selected ).postLink( selectedLink );
   }

   public LinkValue getKnowledgeBaseLink(LinkValue selected)
   {
      return client.getClient(selected).query("knowledgebase", LinkValue.class);
   }
}