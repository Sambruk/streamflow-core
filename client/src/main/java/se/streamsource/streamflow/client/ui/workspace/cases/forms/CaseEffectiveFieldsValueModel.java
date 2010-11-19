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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.representation.Representation;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.resource.caze.EffectiveFieldDTO;
import se.streamsource.streamflow.resource.caze.EffectiveFieldsDTO;

import java.io.IOException;

/**
 * List of contacts for a case
 */
public class CaseEffectiveFieldsValueModel
      implements Refreshable, FormAttachmentDownload
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   EventList<EffectiveFieldDTO> eventList = new TransactionList<EffectiveFieldDTO>( new BasicEventList<EffectiveFieldDTO>( ) );

   public void refresh()
   {
      EventListSynch.synchronize( client.query( "effectivefields", EffectiveFieldsDTO.class ).effectiveFields().get(), eventList );
   }

   public EventList<EffectiveFieldDTO> getEventList()
   {
      return eventList;
   }

   public Representation download( String attachmentId ) throws IOException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( attachmentId );

      return client.queryRepresentation( "download", builder.newInstance() );
   }
}