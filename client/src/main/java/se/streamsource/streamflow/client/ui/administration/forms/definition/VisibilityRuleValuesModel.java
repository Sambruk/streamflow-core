/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.administration.forms.definition;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Uses;
import org.restlet.data.Form;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.administration.form.VisibilityRuleDefinitionValue;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;

/**
 *
 */
public class VisibilityRuleValuesModel
   implements Refreshable
{
   @Uses
   CommandQueryClient client;

   private EventList<String> elements = new BasicEventList<String>();

   public EventList<String> getEventList()
   {
      return elements;
   }

   public void refresh()
   {

      VisibilityRuleDefinitionValue visibilityRuleDefinitionValue = client.query( "rule", VisibilityRuleDefinitionValue.class );
      EventListSynch.synchronize( visibilityRuleDefinitionValue.values().get(), elements );

   }

   public void addElement( String name )
   {
      Form form = new Form();
      form.set("value", name);
      client.postCommand( "addrulevalue", form.getWebRepresentation() );
   }

   public void removeElement( int index )
   {
      Form form = new Form();
      form.set("index", Integer.toString(index));
      client.postCommand( "removerulevalue", form.getWebRepresentation() );
   }

   public void changeElementName( String newName, int index )
   {
      Form form = new Form();
      form.set("name", newName);
      form.set("index", Integer.toString(index));

      client.postCommand( "changerulevaluename", form.getWebRepresentation() );
   }

}
