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

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.SelectionFieldValue;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.roles.NamedIndexDTO;

import java.util.List;

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
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( name );
      try
      {
         client.postCommand( "addselectionelement", builder.newInstance() );
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_add_field, e );
      }
   }

   public void removeElement( int index )
   {
      ValueBuilder<IntegerDTO> builder = vbf.newValueBuilder( IntegerDTO.class );
      builder.prototype().integer().set( index );

      try
      {
         client.postCommand( "removeselectionelement", builder.newInstance() );
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_field, e );
      }
   }

   public void moveElement( String direction, int index )
   {
      ValueBuilder<NamedIndexDTO> builder = vbf.newValueBuilder( NamedIndexDTO.class );
      builder.prototype().index().set( index );
      builder.prototype().name().set( direction );
      try
      {
         client.postCommand( "moveselectionelement", builder.newInstance() );
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_field, e );
      }
   }

   public void changeElementName( String newName, int index )
   {
      ValueBuilder<NamedIndexDTO> builder = vbf.newValueBuilder( NamedIndexDTO.class );
      builder.prototype().name().set( newName );
      builder.prototype().index().set( index );
      try
      {
         client.putCommand( "changeselectionelementname", builder.newInstance() );
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_add_field, e );
      }
   }
}