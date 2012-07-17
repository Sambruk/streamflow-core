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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.dci.value.link.TitledLinksValue;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;

/**
 * JAVADOC
 */
public class FieldCreationModel
{
   @Uses
   private CommandQueryClient client;

   private Module module;

   public FieldCreationModel(@Structure Module module)
   {
      this.module = module;
   }

   @SuppressWarnings("unchecked")
   public EventList<TitledLinkValue> getPossibleFields()
   {
      EventList<TitledLinkValue> possiblefields = new TransactionList<TitledLinkValue>( new BasicEventList<TitledLinkValue>());
      TitledLinksValue titledLinksValue = client.query("possiblefields", TitledLinksValue.class);
      List<TitledLinkValue> values = new ArrayList<TitledLinkValue>();
      // Uggly....
      for (LinkValue linkvalue : titledLinksValue.links().get())
      {
         values.add((TitledLinkValue) linkvalue);
      }
      possiblefields.addAll( values );
      return  possiblefields;
   }

}