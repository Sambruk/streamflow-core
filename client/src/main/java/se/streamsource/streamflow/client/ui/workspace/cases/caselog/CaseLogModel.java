package se.streamsource.streamflow.client.ui.workspace.cases.caselog;

import java.util.Observable;

import org.qi4j.api.injection.scope.Uses;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.general.CaseLogEntryDTO;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;

public class CaseLogModel extends Observable
   implements Refreshable
{

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

}
