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
package se.streamsource.streamflow.client.ui.workspace.cases.caselog;

import java.util.Observable;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.general.CaseLogEntryDTO;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.util.Strings;

public class CaseLogModel extends Observable
   implements Refreshable
{

   @Structure
   private Module module;
   
   private final CommandQueryClient client;

   TransactionList<CaseLogEntryDTO> caselogs = new TransactionList<CaseLogEntryDTO>(new BasicEventList<CaseLogEntryDTO>( ));

   public CaseLogModel(@Uses CommandQueryClient client)
   {
      this.client = client;
   }
   
   public void refresh()
   {
      LinksValue newCaseLogs = client.query( "index", LinksValue.class );
      EventListSynch.synchronize( newCaseLogs.links().get(), caselogs );
   }
   
   public EventList<CaseLogEntryDTO> caselogs()
   {
      return caselogs;
   }

   public void addMessage( String newMessage )
   {
      if (Strings.empty( newMessage ))
         return; // No add

      ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder(StringValue.class);
      builder.prototype().string().set( newMessage );
      client.postCommand( "addmessage", builder.newInstance() );
   }
}
