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
public class RulesModel
      extends LinkValueListModel
{
   public RulesModel()
   {
      relationModelMapping("labelrule", LabelRuleModel.class);
   }

   public EventList<LinkValue> getPossibleLabels()
   {
      return EventListSynch.synchronize(client.query("possiblelabels", LinksValue.class).links().get(), new BasicEventList<LinkValue>());
   }

   public void createLabelRule(LinkValue selectedLink)
   {
      client.postLink(selectedLink);
   }
}
