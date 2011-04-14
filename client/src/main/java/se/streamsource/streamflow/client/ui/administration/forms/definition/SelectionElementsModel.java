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

package se.streamsource.streamflow.client.ui.administration.forms.definition;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.administration.form.FieldValue;
import se.streamsource.streamflow.api.administration.form.SelectionFieldValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.api.administration.form.FieldDefinitionValue;

import java.util.*;

/**
 * JAVADOC
 */
public class SelectionElementsModel
   implements Refreshable
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   private EventList<String> elements = new BasicEventList<String>();

   public EventList<String> getEventList()
   {
      return elements;
   }

   public void refresh()
   {
      try
      {
         FieldDefinitionValue fieldDefinitionValue = client.query( "field", FieldDefinitionValue.class );

         FieldValue field = fieldDefinitionValue.fieldValue().get();
         if (field instanceof SelectionFieldValue)
         {
            SelectionFieldValue selectionField = (SelectionFieldValue) field;
            List<String> elts = selectionField.values().get();
            EventListSynch.synchronize( elts, elements );
         }
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_get_field, e );
      }
   }

   public void addElement( String name )
   {
      Form form = new Form();
      form.set("selection", name);
      client.postCommand( "addselectionelement", form.getWebRepresentation() );
   }

   public void removeElement( int index )
   {
      Form form = new Form();
      form.set("index", Integer.toString(index));
      client.postCommand( "removeselectionelement", form.getWebRepresentation() );
   }

   public void moveElement( String direction, int index )
   {
      Form form = new Form();
      form.set("name", direction);
      form.set("index", Integer.toString(index));
      
      client.postCommand( "moveselectionelement", form.getWebRepresentation() );
   }

   public void changeElementName( String newName, int index )
   {
      Form form = new Form();
      form.set("name", newName);
      form.set("index", Integer.toString(index));

      client.postCommand( "changeselectionelementname", form.getWebRepresentation() );
   }
}