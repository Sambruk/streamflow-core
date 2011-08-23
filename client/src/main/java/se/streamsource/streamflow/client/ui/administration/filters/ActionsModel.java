package se.streamsource.streamflow.client.ui.administration.filters;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.LinkValueListModel;

/**
 * TODO
 */
public class ActionsModel
      extends LinkValueListModel
{
   public ActionsModel()
   {
      relationModelMapping("emailaction", EmailActionModel.class);
   }

   public EventList<LinkValue> getPossibleRecipients()
   {
      return EventListSynch.synchronize(client.query("possiblerecipients", LinksValue.class).links().get(), new BasicEventList<LinkValue>());
   }

   public void createEmailAction(LinkValue selectedLink)
   {
      client.postLink(selectedLink);
   }
}
