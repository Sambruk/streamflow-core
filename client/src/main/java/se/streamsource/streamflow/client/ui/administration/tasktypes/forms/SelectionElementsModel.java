/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.administration.tasktypes.forms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.domain.form.FieldValue;
import se.streamsource.streamflow.domain.form.SingleSelectionFieldValue;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.roles.NamedIndexDTO;

import javax.swing.AbstractListModel;
import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * JAVADOC
 */
public class SelectionElementsModel
      extends AbstractTableModel
   implements Refreshable
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   private List<String> elements;

   public void refresh()
   {
      try
      {
         FieldDefinitionValue fieldDefinitionValue = client.query( "field", FieldDefinitionValue.class );

         FieldValue field = fieldDefinitionValue.fieldValue().get();
         if (field instanceof SingleSelectionFieldValue)
         {
            SingleSelectionFieldValue singleSelectionField = (SingleSelectionFieldValue) field;
            elements = singleSelectionField.values().get();
            fireTableDataChanged();
         }
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_get_field, e );
      }
   }

   public void addElement( )
   {
      try
      {
         client.putCommand( "addselectionelement" );
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
         client.putCommand( "moveselectionelement", builder.newInstance() );
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_field, e );
      }
   }

   public int getRowCount()
   {
      return elements == null ? 0 : elements.size();
   }

   public int getColumnCount()
   {
      return 1;
   }

   public Object getValueAt( int row, int col )
   {
      return elements.get( row );
   }

   @Override
   public void setValueAt( Object o, int row, int col )
   {
      ValueBuilder<NamedIndexDTO> builder = vbf.newValueBuilder( NamedIndexDTO.class );
      builder.prototype().name().set( (String) o );
      builder.prototype().index().set( row );
      try
      {
         client.putCommand( "changeselectionelementname", builder.newInstance() );
         refresh();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_add_field, e );
      }
   }

   @Override
   public String getColumnName( int i )
   {
      return "Selection element";
   }

   @Override
   public Class<?> getColumnClass( int i )
   {
      return String.class;
   }

   @Override
   public boolean isCellEditable( int row, int col )
   {
      return true;
   }
}