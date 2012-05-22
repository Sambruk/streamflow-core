/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryDTO;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogFilterValue;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import static org.qi4j.api.util.Iterables.matchesAny;
import static se.streamsource.dci.value.link.Links.withRel;
import static se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryTypes.*;

public class CaseLogModel extends Observable
   implements Refreshable
{

   @Structure
   private Module module;
   
   private final CommandQueryClient client;

   private ResourceValue resourceValue;

   private List<String> selectedFilters = new ArrayList<String>();
   

   TransactionList<CaseLogEntryDTO> caselogs = new TransactionList<CaseLogEntryDTO>(new BasicEventList<CaseLogEntryDTO>( ));

   public CaseLogModel(@Uses CommandQueryClient client)
   {
      this.client = client;
      CaseLogFilterValue defaultFilters = client.query( "defaultfilters", CaseLogFilterValue.class );
      if (defaultFilters.attachment().get()) selectedFilters.add(attachment.name());
      if (defaultFilters.contact().get()) selectedFilters.add(contact.name());
      if (defaultFilters.conversation().get()) selectedFilters.add(conversation.name());
      if (defaultFilters.custom().get()) selectedFilters.add(custom.name());
      if (defaultFilters.form().get()) selectedFilters.add(form.name());
      if (defaultFilters.system().get()) selectedFilters.add(system.name());
      if (defaultFilters.systemTrace().get()) selectedFilters.add(system_trace.name());
   }
   
   public void refresh()
   {
      resourceValue = client.query();

      LinksValue newCaseLogs = client.query( "list", LinksValue.class, createFilter() );
      EventListSynch.synchronize( newCaseLogs.links().get(), caselogs );

      setChanged();
      notifyObservers( resourceValue );
   }
   
   private CaseLogFilterValue createFilter()
   {
      ValueBuilder<CaseLogFilterValue> builder = module.valueBuilderFactory().newValueBuilder( CaseLogFilterValue.class );
      builder.prototype().attachment().set( selectedFilters.contains( attachment.name() ) );
      builder.prototype().contact().set( selectedFilters.contains( contact.name() ) );
      builder.prototype().conversation().set( selectedFilters.contains( conversation.name() ) );
      builder.prototype().custom().set( selectedFilters.contains( custom.name() ) );
      builder.prototype().form().set( selectedFilters.contains( form.name() ) );
      builder.prototype().system().set( selectedFilters.contains( system.name() ) );
      builder.prototype().systemTrace().set( selectedFilters.contains( system_trace.name() ) );
      return builder.newInstance();
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

   public List<String> getSelectedFilters()
   {
      return selectedFilters;
   }

   public void togglepublish( LinkValue link )
   {
      client.postLink( link );
   }

   public boolean getCommandEnabled( String commandName )
   {
      return matchesAny( withRel( commandName ), resourceValue.commands().get() );
   }

}
