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
package se.streamsource.streamflow.client.ui;

import se.streamsource.dci.restlet.client.CommandQueryClient;

/**
* JAVADOC
*/
public class ContextItem
{
   private String group;
   private String name;
   private String rel;
   private long count;
   private CommandQueryClient client;

   public ContextItem( String group, String name, String rel, long count, CommandQueryClient client )
   {
      this.group = group;
      this.name = name;
      this.rel = rel;
      this.count = count;
      this.client = client;
   }

   public String getGroup()
   {
      return group;
   }

   public String getName()
   {
      return name;
   }

   public String getRelation()
   {
      return rel;
   }

   public long getCount()
   {
      return count;
   }

   public CommandQueryClient getClient()
   {
      return client;
   }

   public void setCount(long count)
   {
      this.count = count;
   }
}
