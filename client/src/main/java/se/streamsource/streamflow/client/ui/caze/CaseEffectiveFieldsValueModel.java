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

package se.streamsource.streamflow.client.ui.caze;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;
import se.streamsource.streamflow.resource.caze.EffectiveFieldDTO;
import se.streamsource.streamflow.resource.caze.EffectiveFieldsDTO;

import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * List of contacts for a case
 */
public class CaseEffectiveFieldsValueModel
      extends AbstractTableModel
      implements Refreshable, EventListener, EventVisitor

{

   String[] columnNames = {
         i18n.text( WorkspaceResources.field_name ),
         i18n.text( WorkspaceResources.field_value ),
         i18n.text( WorkspaceResources.field_submitter ),
         i18n.text( WorkspaceResources.field_date )
   };

   private SimpleDateFormat formatter = new SimpleDateFormat( i18n.text( WorkspaceResources.date_time_format ) );

   @Structure
   ValueBuilderFactory vbf;

   @Uses
   CommandQueryClient client;

   List<EffectiveFieldDTO> effectiveFields = Collections.emptyList();

   EventVisitorFilter eventFilter = new EventVisitorFilter( this, "submittedForm" );

   public void refresh()
   {
      try
      {
         effectiveFields = client.query( "effectivefields", EffectiveFieldsDTO.class ).effectiveFields().get();
         fireTableStructureChanged();
      } catch (Exception e)
      {
         throw new OperationException( CaseResources.could_not_refresh, e );
      }
   }

   public int getRowCount()
   {
      return effectiveFields == null ? 0 : effectiveFields.size();
   }

   public int getColumnCount()
   {
      return columnNames.length;
   }

   public Object getValueAt( int row, int col )
   {
      EffectiveFieldDTO value = effectiveFields.get( row );

      switch (col)
      {
         case 0:
            return value.fieldName().get();
         case 1:
            return value.fieldValue().get();
         case 2:
            return value.submitter().get();
         case 3:
            return formatter.format( value.submissionDate().get() );
      }
      return null;
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

   public void notifyEvent( DomainEvent event )
   {
      eventFilter.visit( event );
   }

   public boolean visit( DomainEvent event )
   {
      if (client.getReference().getParentRef().getLastSegment().equals( event.entity().get() ))
      {
         Logger.getLogger( "workspace" ).info( "Refresh effective field" );
         refresh();
      }

      return false;
   }
}