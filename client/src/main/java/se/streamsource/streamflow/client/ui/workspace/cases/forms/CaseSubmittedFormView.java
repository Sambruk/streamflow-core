/*
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

package se.streamsource.streamflow.client.ui.workspace.cases.forms;

import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventJXTableModel;
import org.jdesktop.swingx.JXTable;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.util.DateFunctions;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.ToolTipTableCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.resource.caze.FieldDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.util.Strings;

import javax.swing.JScrollPane;
import java.text.SimpleDateFormat;

/**
 * JAVADOC
 */
public class CaseSubmittedFormView
      extends JScrollPane
{
   private EventJXTableModel<FieldDTO> tableModel;


   public CaseSubmittedFormView(@Uses CommandQueryClient client, @Uses IntegerDTO index, @Structure ObjectBuilderFactory obf)
   {
      CaseSubmittedFormModel model = obf.newObjectBuilder( CaseSubmittedFormModel.class ).use(client, index).newInstance();

      TableFormat<FieldDTO> fieldDTOTableFormat = new TableFormat<FieldDTO>()
      {
         public int getColumnCount()
         {
            return 2;
         }

         public String getColumnName( int i )
         {
            return new String[]{i18n.text( WorkspaceResources.field_name ), i18n.text( WorkspaceResources.field_value )}[i];
         }

         public Object getColumnValue( FieldDTO field, int column )
         {
            switch (column)
            {
               case 0:
                  return field.field().get();
               default:
                  if (DateFieldValue.class.getName().equals( field.fieldType().get() ) &&
                        Strings.notEmpty( field.value().get() ))
                  {
                     return new SimpleDateFormat( i18n.text( WorkspaceResources.date_time_format ) ).format( DateFunctions.fromString( field.value().get() ) );
                  } else
                  {
                     return field.value().get();
                  }
            }
         }
      };

      tableModel = new EventJXTableModel<FieldDTO>( model.getEventList(), fieldDTOTableFormat );

      JXTable fieldValues = new JXTable(tableModel);
      fieldValues.setDefaultRenderer( Object.class, new ToolTipTableCellRenderer() );
      setViewportView( fieldValues );

      new RefreshWhenVisible(this, model);
   }
}