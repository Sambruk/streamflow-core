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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.domain.form.DateFieldValue;
import se.streamsource.streamflow.resource.caze.EffectiveFieldDTO;
import se.streamsource.streamflow.resource.caze.EffectiveFieldsDTO;

import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * List of contacts for a case
 */
public class CaseEffectiveFieldsValueModel
      implements Refreshable
{
   @Uses
   CommandQueryClient client;

   EventList<EffectiveFieldDTO> eventList = new BasicEventList<EffectiveFieldDTO>( );

   public void refresh()
   {
      EventListSynch.synchronize( client.query( "effectivefields", EffectiveFieldsDTO.class ).effectiveFields().get(), eventList );
   }

   public EventList<EffectiveFieldDTO> getEventList()
   {
      return eventList;
   }
}