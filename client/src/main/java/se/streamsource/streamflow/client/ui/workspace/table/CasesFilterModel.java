package se.streamsource.streamflow.client.ui.workspace.table;

import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.injection.scope.Uses;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.util.Refreshable;
import ca.odell.glazedlists.BasicEventList;

public class CasesFilterModel implements Refreshable
{
   @Uses
   CommandQueryClient client;
   
   BasicEventList<LinkValue> possibleLabels = new BasicEventList<LinkValue>();
   List<LinkValue> selectedLabels = new ArrayList<LinkValue>();
   
   public void refresh()
   {
      LinksValue listValue = client.query( "possiblelabels",
            LinksValue.class );
      possibleLabels.addAll(listValue.links().get());
   }

   public BasicEventList<LinkValue> getPossibleLabels()
   {
      return possibleLabels;
   }
   
   public List<LinkValue> getSelectedLabels()
   {
      return selectedLabels;
   }
}
