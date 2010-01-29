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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.resource.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.task.FieldDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;

import javax.swing.table.AbstractTableModel;

public class TaskSubmittedFormModel
      extends AbstractTableModel
{

   String[] columnNames = {i18n.text( WorkspaceResources.field_name ), i18n.text( WorkspaceResources.field_value )};

   private SubmittedFormDTO form;

   public TaskSubmittedFormModel( @Uses CommandQueryClient client,
                                  @Uses IntegerDTO index)
   {
      try
      {
         form = client.query( "submittedform", index ,SubmittedFormDTO.class );
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_get_submitted_form, e );
      }
   }

   public int getRowCount()
   {
      return form.values().get().size();
   }

   public int getColumnCount()
   {
      return 2;
   }

   public Object getValueAt( int row, int column )
   {
      FieldDTO field = form.values().get().get( row );
      switch (column)
      {
         case 0:
            return field.field().get();
         default:
            return field.value().get();
      }
   }

   @Override
   public boolean isCellEditable( int rowIndex, int columnIndex )
   {
      return false;
   }

   @Override
   public String getColumnName( int i )
   {
      return columnNames[i];
   }
}