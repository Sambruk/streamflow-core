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
import org.apache.commons.collections.CollectionUtils;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.specification.Specification;
import org.restlet.data.Form;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.form.VisibilityRuleDefinitionValue;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;

import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.util.Iterables.*;

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

   public void addElements( LinksValue selectedLinks )
   {
      for( LinkValue link : selectedLinks.links().get() )
      {
         Form form = new Form();
         form.set("value", link.text().get());
         client.postCommand( "addrulevalue", form.getWebRepresentation() );
      }
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
      form.set("value", newName);
      form.set("index", Integer.toString(index));

      client.postCommand( "changerulevaluename", form.getWebRepresentation() );
   }

   /**
    * Fetches any possible rule values, especially if the target rule is a SelectionField.
    * The result is filtered by already set values. If the target rule is not a SelectionField
    * the EventList will be empty.
    * @return An EventList containing possible rule values.
    */
   public EventList<LinkValue> possiblePredefinedRuleValues()
   {
      EventList<LinkValue> possibleRuleValues = new BasicEventList<LinkValue>(  );
      LinksValue available = client.query( "possiblerulevalues", LinksValue.class );

      List<LinkValue> linkValues = new ArrayList<LinkValue>();
      CollectionUtils.addAll( linkValues, filter( new Specification<LinkValue>()
      {
         public boolean satisfiedBy( LinkValue link )
         {
            return !elements.contains( link.text().get() );
         }
      }, available.links().get() ).iterator() );

      EventListSynch.synchronize( linkValues, possibleRuleValues );

      return possibleRuleValues;
   }

}
