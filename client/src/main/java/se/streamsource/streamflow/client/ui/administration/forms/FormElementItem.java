/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.administration.forms;

import se.streamsource.dci.restlet.client.CommandQueryClient;

/**
* JAVADOC
*/
public class FormElementItem
{
   private String name;
   private String rel;
   private CommandQueryClient client;

   public FormElementItem( String name, String rel, CommandQueryClient client )
   {
      this.name = name;
      this.rel = rel;
      this.client = client;
   }

   public String getName()
   {
      return name;
   }

   public String getRelation()
   {
      return rel;
   }

   public CommandQueryClient getClient()
   {
      return client;
   }
}
