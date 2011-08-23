package se.streamsource.streamflow.client.ui.administration.filters;

import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;

/**
 * Match label filter rule
 */
public class LabelRuleView
   extends JPanel
{
   public LabelRuleView(@Service ApplicationContext context, @Uses LabelRuleModel model)
   {
      JLabel label = new JLabel(i18n.text(AdministrationResources.match_label, model.getIndex().getFirstValue("label")), JLabel.LEFT);
      add(label);
   }
}
